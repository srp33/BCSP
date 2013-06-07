import glob,os,sys,time,posix

from numpy import *
from numpy.linalg import *

from em import EM
#from em2 import EM
from mycel import MyCEL
import utilities

class Normalize(object):
    """Normalize the probe's raw_intensity based on its sequence.
    See 'http://www.pnas.org/content/103/33/12457.abstract?ck=nck' for description of this algorithm.
    """
    def __init__(self):
        self.code= {'A':0,'a':0,'G':1,'g':1,'C':2,'C':2,'T':3,'t':3}

        #use a subset of probes to estimate overall parameters.(to save time)
        self.N = 50000

    def _encode(self,seq):
        return [self.code[s] for s in seq]

    def _sig(self,y,m):
        resid=y-m
        s2=sqrt(dot(resid,resid)/size(y))
        return s2

    def _sample(self,total,start=1):
        """
        @parameters:
        total: integer, the total number
        start: integer, the beginning number

        @return [1,3,5,7,8, ....]

        """
        interval = total/self.N
        if interval <= 1:
            interval = 1
        return [i for i in range(start,total,interval)]

    def _quantile_normalize(self,x):
        xr = sort(x,axis=0)
        xm = mean(x,axis=1)
        return xm[argsort(x,axis=0)]

    def _design_matrix(self,PMProbe):
        """DesignMatrix(PMProbe): make design matrix from PMProbe
        PMProbe: probe sequence Int8 array,
        Constructs a 80 pars X-matrix for the model
        3 * 25 'ACG' postions + 4 * 2 'ACGT' count and count square
        """
        x = zeros((PMProbe.shape[0],80), 'f')
        x[:,0] = sum(PMProbe == self.code['T'],1).astype('float32')
        j = 1
        for ibase in 'ACG':
            x[:, j:j+25] = (PMProbe == self.code[ibase])
            j += 25
        for ibase in 'ACGT':
            count = sum(PMProbe == self.code[ibase], 1).astype('float32')
            x[:, j] = count**2
            j += 1
        return x

    def normalize(self,probe_int,probe_seq, modelProbes=None):
        """
        @parameter:
         probe_int {probe_id:probe_intensity, ... }
         probe_seq {probe_id:probe_sequence, ... }

        @return:
         {probe_id:normalized_probe_intensity, ...}

        """

        probe_ids = probe_seq.keys() #[probe_id]
        pseq = []
        pint = []
        for p in probe_ids:
            pseq.append(probe_seq[p])
            pint.append(probe_int[p])
        mx = self._design_matrix(array([self._encode(v) for v in pseq]))
        total_probes = len(probe_ids)
        my = log2(array((pint)))
        model = EM()
        indices = self._sample(total_probes)
        #selected = [probe_ids[i] for i in indices]

        if modelProbes != None:
            modelProbeIndexDict = {}
            for x in enumerate(probe_seq.keys()):
                modelProbeIndexDict[x[1]] = x[0]

            modelProbeIndices = [modelProbeIndexDict[probe] for probe in modelProbes if modelProbeIndexDict.has_key(probe)]
            indices = list(set(indices) & set(modelProbeIndices))

        ####b1 = model.EM_Mix(my[indices,],mx[indices,])
        nbins=25  ### evan added 3/11/2011
        b1 = model.EM_vMix(my[indices,],mx[indices,],bins=nbins) ### evan changed 3/11/2011 
        ret = {}
        y_predicted=dot(mx,b1)
        #binsize=mx.shape[0]/10
        binsize = 5000
        nGroups = int(ceil(size(my)/binsize))
        index   = argsort(y_predicted)#the position of each element if sorted
        y_norm = zeros(size(my),'f')
        for i in arange(nGroups):
            tmp=index[(binsize*i):min([binsize*i+binsize,size(my)])]
            tmpSd=self._sig(my[tmp],y_predicted[tmp])
            y_norm[tmp]=((my[tmp]-y_predicted[tmp])/tmpSd).tolist()

        model.assign_bin(y_predicted,bins=nbins) ### evan added 3/11/2011
        gam = model.vresp(my,mx) ### evan changed 3/11/2011
        for i, pid in enumerate(probe_ids):
            ret[pid] = y_norm[i], gam[i,1]

        return ret

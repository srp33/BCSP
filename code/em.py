"""
E-M model to estimate model parameters.
"""
from numpy import *
import numpy
from numpy.linalg import *
#from scipy.optimize import fmin_bfgs as optim

class EM:

    def __init__(self):
        pass

    def dnorm(self,y,m,s2):
        """
        y - [1,2,3,4,5]
        m - mean(y)
        s2 = std(y)
        """
        fy=1/(sqrt(2*pi*s2))*exp(-1/(2*s2)*(y-m)**2)
        return fy
    
    def resp(self,y,X):
        L0=self.dnorm(y,dot(X,self.b1),self.s1)
        L1=self.dnorm(y,dot(X,self.b2),self.s2)

        gam=zeros((shape(y)[0],2),'f')
        #gam[:,0] measure the background.
        gam[:,0]=self.p[0]*L0/(self.p[0]*L0+self.p[1]*L1)
        gam[:,1]=1-gam[:,0]
        return gam

    def beta(self,y,X,gam):
        sqgam=sqrt(gam)
        Xw=transpose(sqgam*transpose(X))
        yw=sqgam*y
        beta=dot(dot(inv(dot(transpose(Xw), Xw)),transpose(Xw)), yw)
        return beta

    def sig(self,y,X,b,gam):
        resid=y-dot(X,b)
        s2=dot(resid*gam,resid)/sum(gam)
        #print s2,type(s2),s2.shape
        return array([s2])

    def EM_Mix(self,y,X,p=.5,conv=.01):
        #Starting values
        print "Starting EM"
        self.gam = zeros((len(y),2),'f')
        quan=sort(y)[int(p*len(y))-1]
        self.gam[:,0]=y<=quan
        #print 'self.gam:',self.gam[:,0].shape,self.gam[:,0]
        self.gam[:,1]=y>quan
        #self.p=average(self.gam)
        self.p =mean(self.gam,axis=0) #numpy
        self.b1=self.beta(y,X,self.gam[:,0])
        self.b2=self.beta(y,X,self.gam[:,1])
        self.s1=self.sig(y,X,self.b1,self.gam[:,0])
        self.s2=self.sig(y,X,self.b2,self.gam[:,1])
        #self.s2=self.s1
        theta_old=concatenate((self.p,self.b1,self.s1,self.b2,self.s2))
        #print 'Starting mixing p:',self.p
        it=0
        c=1
        while c>conv:
            # Expectation Step:
            self.gam=self.resp(y,X)
      
            #M-Step
            #self.p=average(self.gam)
            self.p =mean(self.gam,axis=0) #numpy
            self.b1=self.beta(y,X,self.gam[:,0])            
            self.b2=self.beta(y,X,self.gam[:,1])
            self.s1=self.sig(y,X,self.b1,self.gam[:,0])
            self.s2=self.sig(y,X,self.b2,self.gam[:,1])
    
            c=max(abs(concatenate((self.p,self.b1,self.s1,self.b2,self.s2))-theta_old)/theta_old)
            theta_old=concatenate((self.p,self.b1,self.s1,self.b2,self.s2))
            #if it%25==0:
            #    print 'For iteration',it,'p is', self.p
            it+=1
        print 'Converged in', it,'iterations. Proportion of background probes:', self.p[0]
        #print 'X',X
        #print 'self.b2',self.b2
        print 'self.b1',self.b1
        #print 'self.s2',self.s2
        #print 'self.s1',self.s1
        return self.b1
    
    def uresp(self,y,X):
        L0=self.dnorm(y,dot(X,self.b),self.s)
        L1=numpy.array([0.0 for i in range(len(y))])
        L1[y>dot(X,self.b)]=1/(self.Z)  # uniform
        gam=zeros((shape(y)[0],2),'f')
        gam[:,0]=self.p[0]*L0/(self.p[0]*L0+self.p[1]*L1)          #gam[:,0] measure the background.
        gam[:,1]=1-gam[:,0]
        return gam

    def ubetaLik(self,b,ystar,X1,gam):
        return -1/(2*self.s)*sum(gam*(ystar-X1*b)**2)-sum((1-gam)*log(self.M-X1*b))

    def ubeta(self,y,X,gam,conv=.05,it=3):
        for j in range(it):
            for i in range(len(self.b)):
                X1=X[:,i]
                Xstar=delete(X,s_[i],1)
                bstar=delete(self.b,i)
                ystar=y-dot(Xstar,bstar)
                a=sum(gam*(X1**3))
                b=-sum(gam*(X1**2)*(ystar+self.M))
                c=(self.M*sum(gam*X1*ystar)+self.s*sum((1-gam)*X1))[0]
                b1=((-b+sqrt(b**2-4*a*c))/(2*a))
                b2=((-b-sqrt(b**2-4*a*c))/(2*a))
                if self.ubetaLik(b1,ystar,X1,gam)>self.ubetaLik(b2,ystar,X1,gam):
                    self.b[i]=b1
                else:
                    self.b[i]=b2
                #self.b[i]=sum(gam*X1*ystar)/sum(gam*X1*X1)
            
    def EM_uMix(self,y,X,p=.5,conv=.01):
        #Starting values
        print "Starting EM"
        self.gam = zeros((len(y),2),'f')
        quan=sort(y)[int(p*len(y))-1]
        self.gam[:,0]=y<=quan
        self.gam[:,1]=y>quan
        self.p=mean(self.gam,axis=0) #numpy
        self.b=self.beta(y,X,self.gam[:,0])
        self.s=self.sig(y,X,self.b,self.gam[:,0])
        self.Z=max(y-dot(X,self.b))

        theta_old=concatenate((self.p,self.b,self.s,numpy.array([self.Z])))
        #print 'Starting mixing p:',self.p
        it=0
        c=1
        while c>conv:
            # Expectation Step:
            self.gam=self.uresp(y,X)
      
            #M-Step
            self.p=mean(self.gam,axis=0) #numpy
            
            #self.ubeta(y,X,self.gam[:,0])            
            self.b=self.beta(y,X,self.gam[:,0])            
            self.s=self.sig(y,X,self.b,self.gam[:,0])
            self.Z=max(y-dot(X,self.b))
            
            c=max(abs(concatenate((self.p,self.b,self.s,numpy.array([self.Z])))-theta_old)/theta_old)
            theta_old=concatenate((self.p,self.b,self.s,numpy.array([self.Z])))
            #if it%25==0:
            #    print 'For iteration',it,'p is', self.p
            it+=1
        print 'Converged in', it,'iterations. Proportion of background probes:', self.p[0]
        return self.b


    def vresp(self,y,X):
        vars0=numpy.array([self.s1[i] for i in self.bin])
        L0=self.dnorm(y,dot(X,self.b1),vars0)
        vars1=numpy.array([self.s2[i] for i in self.bin])
        L1=self.dnorm(y,dot(X,self.b2),vars1)

        gam=zeros((shape(y)[0],2),'f')
        #gam[:,0] measure the background.
        gam[:,0]=self.p[0]*L0/(self.p[0]*L0+self.p[1]*L1)
        gam[:,1]=1-gam[:,0]
        return gam

    def vbeta(self,y,X,gam,s2):
        vars=numpy.sqrt(numpy.array([s2[i] for i in self.bin]))
        sqgam=sqrt(gam)
        Xw=transpose(1/vars*sqgam*transpose(X))
        yw=1/vars*sqgam*y
        beta=dot(dot(inv(dot(transpose(Xw), Xw)),transpose(Xw)), yw)
        return beta

    def vsig(self,y,X,b,gam,bins):
        s2=numpy.zeros(bins)+1
        for i in arange(bins):
            ystar=y[self.bin==i]
            Xstar=X[self.bin==i,:]
            gamstar=gam[self.bin==i]+.01
            resid=ystar-dot(Xstar,b)
            s2[i]=dot(resid*gamstar,resid)/sum(gamstar)
        return s2

    def assign_bin(self, y, bins):
        sy=numpy.sort(y);n=len(y)
        quans=[sy[int(n*i/bins)] for i in range(1,bins)]
        self.bin = numpy.zeros(n,'i')
        for i in range(bins-1):
            self.bin[y>quans[i]]+=1
        
    def EM_vMix(self,y,X,p=.5,bins=10,conv=.01):
        #Starting values
        print "Starting EM"
        self.assign_bin(y,bins)

        self.gam = zeros((len(y),2),'f')
        quan=sort(y)[int(p*len(y))-1]
        self.gam[:,0]=y<=quan
        self.gam[:,1]=y>quan
        
        self.p =mean(self.gam,axis=0) #numpy
        self.b1=self.beta(y,X,self.gam[:,0])
        self.b2=self.beta(y,X,self.gam[:,1])
        self.s1=self.vsig(y,X,self.b1,self.gam[:,0],bins)
        self.s2=self.vsig(y,X,self.b2,self.gam[:,1],bins)
        
        theta_old=concatenate((self.p,self.b1,self.s1,self.b2,self.s2))
        it=0
        c=1
        while c>conv and it<1000:
            #print it
            # Expectation Step:
            self.gam=self.vresp(y,X)
      
            #M-Step
            #self.p=average(self.gam)
            self.p =mean(self.gam,axis=0) #numpy
            self.b1=self.vbeta(y,X,self.gam[:,0],self.s1)
            self.assign_bin(dot(X,self.b1),bins) ## assign bins based on background expected value
            self.b2=self.vbeta(y,X,self.gam[:,1],self.s2)
            self.s1=self.vsig(y,X,self.b1,self.gam[:,0],bins)
            self.s2=self.vsig(y,X,self.b2,self.gam[:,1],bins)
    
            c=max(abs(concatenate((self.p,self.b1,self.s1,self.b2,self.s2))-theta_old)/theta_old)
            theta_old=concatenate((self.p,self.b1,self.s1,self.b2,self.s2))
            it+=1
        print 'Converged in', it,'iterations. Proportion of background probes:', self.p[0]
        return self.b1


'''
from em import *
import numpy

y=numpy.array([2.643254, 4.033662, 6.312421, 4.539054, 6.416255, 2.535445, 4.730555, 6.530505, 9.602606, 11.02814, 3.210855, 6.368144, 7.803356, 6.077369, 7.816515, 8.289626, 9.64034, 8.603153, 6.890807, 10.25251])

X=numpy.array([[1, 1, 1], [1, 2, 2], [1, 3, 3], [1, 4, 4], [1, 5, 5], [1, 6, 1], [1, 7, 2], [1, 8, 3], [1, 9, 4], [1, 10, 5], [1, 1, 1], [1, 2, 2], [1, 3, 3], [1, 4, 4], [1, 5, 5], [1, 6, 1], [1, 7, 2], [1, 8, 3], [1, 9, 4], [1, 10, 5]])

example=EM()

gam1=example.EM_Mix(y,X)
gam2=example.EM_uMix(y,X)
gam3=example.EM_vMix(y,X)

gam1
gam2
gam3


#debug:
self=example
bins=3;p=.5
self.assign_bin(y,bins)

self.gam = zeros((len(y),2),'f')
quan=sort(y)[int(p*len(y))-1]
self.gam[:,0]=y<=quan
self.gam[:,1]=y>quan
        
self.p =mean(self.gam,axis=0) #numpy
self.b1=self.beta(y,X,self.gam[:,0])
self.b2=self.beta(y,X,self.gam[:,1])
self.s1=self.vsig(y,X,self.b1,self.gam[:,0],bins)
self.s2=self.vsig(y,X,self.b2,self.gam[:,1],bins)
        
theta_old=concatenate((self.p,self.b1,self.s1,self.b2,self.s2))

self.gam=self.vresp(y,X)
      
            #M-Step
self.p =mean(self.gam,axis=0) #numpy
self.b1=self.vbeta(y,X,self.gam[:,0],self.s1)
self.assign_bin(dot(X,self.b1),bins) ## assign bins based on background expected value
self.b2=self.vbeta(y,X,self.gam[:,1],self.s2)
self.s1=self.vsig(y,X,self.b1,self.gam[:,0],bins)
self.s2=self.vsig(y,X,self.b2,self.gam[:,1],bins)

'''

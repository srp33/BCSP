#Read the Affymetrix's Version 4 CEL file.
#See http://www.stat.lsa.umich.edu/~kshedden/Courses/Stat545/Notes/AffxFileFormats/cel.html for details of the format.

import sys,os,struct,time

class MyCEL(object):
    def _read_integer(self,file):
        return  struct.unpack('i', file.read(4))[0]

    def _read_char(self,file,length):
        return  file.read(length)

    def _read_dword(self,file):
        return  struct.unpack('I', file.read(4))[0]

    def _read_float(self,file):
        return  struct.unpack('f', file.read(4))[0]

    def _read_short(self,file):
        return  struct.unpack('h', file.read(2))[0]

    def read_cel(self, fcel, coord):
        """Read the .CEL file.
        @param:
            fcel - filename.cel
            coord - {(row,col):probe_id,...}
        @return
            {probe_id:[raw_intensity,probe_sequence], ... }
        """
        entry = {}
        exp = []

        f = open(fcel,"rb") #read binary
        subgrids = []
        magic_number = self._read_integer(f) #always 64
        version_number = self._read_integer(f) #always 4
        num_columns = self._read_integer(f)
        num_rows = self._read_integer(f)
        num_cells = self._read_integer(f)#rows*columns
        header_length = self._read_integer(f)#length of the header
        header = self._read_char(f,header_length)
        algorithm_name_length = self._read_integer(f)
        algorithm_name = self._read_char(f,algorithm_name_length)
        algorithm_parameters_length = self._read_integer(f)
        algorithm_parameters = self._read_char(f,algorithm_parameters_length)
        cell_margin = self._read_integer(f)
        num_outlier_cells = self._read_dword(f)
        num_masked_cells = self._read_dword(f)
        num_subgrids = self._read_integer(f)

        for c in xrange(num_columns):
            for r in xrange(num_rows):
                #intensity, standard deviation, pixel count
                cell_entries = (self._read_float(f),self._read_float(f),self._read_short(f))
                probe_id = coord.get((r,c)) #probe_id, probe_seq
                if probe_id:
                    entry[probe_id] = int(cell_entries[0])

        return entry

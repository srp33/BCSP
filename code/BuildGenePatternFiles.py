import os, sys, glob, shutil
import utilities

## The input file should contain samples as columns and variables as rows
inFilePath = sys.argv[1]
classesFilePath = sys.argv[2]
outGctFilePath = sys.argv[3]
outClsFilePath = sys.argv[4]

data = utilities.readMatrixFromFile(inFilePath)

samples = data.pop(0)
if len(samples) == len(data[0]):
    samples.pop(0)

classesDict = {}
for row in utilities.readMatrixFromFile(classesFilePath):
    if row[0] in samples:
        classesDict[row[0]] = row[1]

uniqueClasses = sorted(list(set(classesDict.values())))
variables = [x[0] for x in data]

outGctFile = open(outGctFilePath, 'w')
outGctFile.write("#1.2\n")
outGctFile.write("%i\t%i\n" % (len(variables), len(classesDict)))
outGctFile.write("NAME\tDescription\t")
outGctFile.write("\t".join([sample for sample in samples if classesDict.has_key(sample)]) + "\n")

for row in data:
    outGctFile.write("%s\t%s\t" % (row[0], row[0]))
    values = [row[samples.index(sample)+1] for sample in samples if classesDict.has_key(sample)]
    outGctFile.write("\t".join(values) + "\n")

outGctFile.close()

outClsFile = open(outClsFilePath, 'w')
outClsFile.write("%i %i 1\n" % (len(classesDict), len(set(classesDict.values()))))
outClsFile.write("# %s\n" % " ".join(uniqueClasses))
outClsFile.write(" ".join([str(uniqueClasses.index(classesDict[sample])) for sample in samples if classesDict.has_key(sample)]) + "\n")
outClsFile.close()

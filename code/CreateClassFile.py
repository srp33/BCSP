import os, sys, glob
import utilities

inFilePath = sys.argv[1]
valueColumnIndex = int(sys.argv[2])
queryColumnIndex = int(sys.argv[3])
classQueryMeta = sys.argv[4]
outFilePath = sys.argv[5]

classQueryDict = {}
for x in classQueryMeta.split(";"):
    y = x.split("=")
    for z in y[1].split(","):
        classQueryDict[z] = y[0]

data = utilities.readMatrixFromFile(inFilePath)
data = [x for x in data if x[0][0] != "#"]
data.pop(0)

outFile = open(outFilePath, 'w')
for row in data:
    queryValue = row[queryColumnIndex]
    dataValue = row[valueColumnIndex]

    if queryValue in classQueryDict.keys():
        outFile.write("%s\t%s\n" % (dataValue, classQueryDict[queryValue]))
outFile.close()

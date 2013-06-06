import os, sys, glob, shutil
import utilities

inFilePath = sys.argv[1]
keyColumnIndex = int(sys.argv[2])
valueColumnIndex = int(sys.argv[3])
outFilePath = sys.argv[4]

data = utilities.readMatrixFromFile(inFilePath)
data.pop(0)

keyValueDict = {}
for row in data:
    if len(row) > keyColumnIndex and len(row) > valueColumnIndex:
        key = row[keyColumnIndex]
        value = row[valueColumnIndex]
        if key != "":
            keyValueDict[key] = keyValueDict.setdefault(key, []) + [value]

outFile = open(outFilePath, 'w')
for key in sorted(keyValueDict.keys()):
    value = ",".join(list(set(keyValueDict[key])))
    outFile.write("%s\t%s\n" % (key, value))
outFile.close()

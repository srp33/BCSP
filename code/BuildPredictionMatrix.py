import os, sys, glob
from utilities import *

inFilePattern = sys.argv[1]
outFilePath = sys.argv[2]

dataDict = {}

for inFilePath in glob.glob(inFilePattern):
    inFileData = readMatrixFromFile(inFilePath)
    inFileData.pop(0)

    if len(dataDict) == 0:
        for row in inFileData:
            dataDict[row[0]] = {}

    for row in inFileData:
        dataDict[row[0]][os.path.basename(inFilePath)] = row[-1]

samples = sorted(dataDict.keys())

pathways = set()
for sample in dataDict:
    pathways.update(dataDict[sample].keys())
pathways = sorted(list(pathways))

outData = [[""] + samples]
for pathway in pathways:
    outData.append([pathway] + [dataDict[sample][pathway] for sample in samples])

writeMatrixToFile(outData, outFilePath)

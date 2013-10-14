import os, sys, glob
import utilities

inFilePath = sys.argv[1]
hasHeader = sys.argv[2] == "True"
columnIndex = int(sys.argv[3])
decreasing = sys.argv[4] == "True"
outFilePath = sys.argv[5]

data = utilities.readMatrixFromFile(inFilePath)

header = None
if hasHeader:
    header = data.pop(0)

values = [float(x[columnIndex]) for x in data]
ranks = utilities.rankSmart(values, decreasing=decreasing, ties="average")

outData = []
for i in range(len(data)):
    outData.append(data[i] + [str(ranks[i])])

if header != None:
    outData.insert(0, header)

utilities.writeMatrixToFile(outData, outFilePath)

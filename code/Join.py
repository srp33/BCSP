import os, sys, glob
import utilities

inFilePath1 = sys.argv[1]
inFilePath2 = sys.argv[2]
joinColumnIndex1 = int(sys.argv[3])
joinColumnIndex2 = int(sys.argv[4])
defaultValue = sys.argv[5]
outFilePath = sys.argv[6]
outputJoinColumn = sys.argv[7] == "True"

data1 = utilities.readMatrixFromFile(inFilePath1)
data2 = utilities.readMatrixFromFile(inFilePath2)

dataDict1 = {}
dataDict2 = {}

for row in data1:
    dataDict1[row[joinColumnIndex1]] = [row[i] for i in range(len(row)) if i != joinColumnIndex1]

for row in data2:
    dataDict2[row[joinColumnIndex2]] = [row[i] for i in range(len(row)) if i != joinColumnIndex2]

allKeys = set(dataDict1.keys()) | set(dataDict2.keys())

maxRowLength1 = max([len(dataDict1[key]) for key in dataDict1.keys()])
maxRowLength2 = max([len(dataDict2[key]) for key in dataDict2.keys()])

outData = []
for joinValue in allKeys:
    row1 = [defaultValue for i in range(maxRowLength1)]
    row2 = [defaultValue for i in range(maxRowLength2)]

    if joinValue in dataDict1.keys():
        row1 = dataDict1[joinValue]
    if joinValue in dataDict2.keys():
        row2 = dataDict2[joinValue]

    out = []
    if outputJoinColumn:
        out.append(joinValue)

    out += row1 + row2

    outData.append(out)

utilities.writeMatrixToFile(outData, outFilePath)

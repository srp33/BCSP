import os, sys, glob
import utilities
from operator import itemgetter, attrgetter

inFilePath = sys.argv[1]
columnIndex = int(sys.argv[2])
reverse = sys.argv[3] == "reverse"
numHeaderRows = int(sys.argv[4])
numeric = sys.argv[5] == "numeric"
outFilePath = sys.argv[6]

data = utilities.readMatrixFromFile(inFilePath)

headerRows = []
for i in range(numHeaderRows):
    headerRows.append(data.pop(0))

if numeric:
    for i in range(len(data)):
        data[i][columnIndex] = float(data[i][columnIndex])

data.sort(key=itemgetter(columnIndex), reverse=reverse)
data = headerRows + data

utilities.writeMatrixToFile(data, outFilePath)

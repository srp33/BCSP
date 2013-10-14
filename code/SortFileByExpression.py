import os, sys, glob, math
import utilities
from operator import itemgetter, attrgetter

inFilePath = sys.argv[1]
expression = sys.argv[2]
reverse = sys.argv[3] == "reverse"
numHeaderRows = int(sys.argv[4])
outFilePath = sys.argv[5]

data = utilities.readMatrixFromFile(inFilePath)

headerRows = []
for i in range(numHeaderRows):
    headerRows.append(data.pop(0))

data = map(lambda x: eval("x + [" + expression + "]"), data)
data.sort(key=itemgetter(len(data[0])-1), reverse=reverse)
data = [x[:-1] for x in data]

utilities.writeMatrixToFile(headerRows + data, outFilePath)

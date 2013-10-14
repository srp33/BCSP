import os, sys, glob
from operator import itemgetter, attrgetter

inFilePath = sys.argv[1]

inFile = open(inFilePath)
inFile.readline()
inFile.readline()

data = [line.rstrip().split("\t") for line in inFile]

inFile.close()

for i in range(len(data)):
    data[i][0] = int(data[i][0])
    data[i][1] = float(data[i][1])

data.sort(key=itemgetter(1), reverse=True)

print data[0][0]

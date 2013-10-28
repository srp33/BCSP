import os, sys, glob

inFilePath = sys.argv[1]
prefix = sys.argv[2]
numHeaderLines = int(sys.argv[3])
outFilePath = sys.argv[4]

outLines = []

outFile = open(outFilePath, 'w')
inFile = open(inFilePath)

for i in range(numHeaderLines):
    outFile.write(inFile.readline())

for line in inFile:
    outFile.write(prefix + line)

inFile.close()
outFile.close()

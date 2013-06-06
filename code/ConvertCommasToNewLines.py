import os, sys, glob

inFilePath = sys.argv[1]

outData = []
for line in file(inFilePath):
    outData.append(line.rstrip().replace(",", "\n"))

outFile = open(inFilePath, 'w')
for line in outData:
    outFile.write(line)
outFile.close()

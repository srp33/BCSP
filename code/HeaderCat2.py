import os, sys, glob

inFilePath1 = sys.argv[1]
inFilePath2 = sys.argv[2]
outFilePath = sys.argv[3]

outFile = open(outFilePath, 'w')

for line in file(inFilePath1):
    outFile.write(line)

inFile2 = open(inFilePath2)
inFile2.readline()

for line in inFile2:
    outFile.write(line)

inFile2.close()

outFile.close()

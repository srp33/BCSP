import os, sys, glob

inFilePath = sys.argv[1]
outFilePath = sys.argv[2]

outFile = open(outFilePath, 'w')

for line in file(inFilePath):
    outFile.write(line.rstrip() + "\n")

outFile.close()

import os, sys, glob
import utilities

inFilePath = sys.argv[1]
outFilePath = sys.argv[2]

outFile = open(outFilePath, 'w')

for line in file(inFilePath):
    if len(line.strip()) == 0:
        continue

    outFile.write(line)

outFile.close()

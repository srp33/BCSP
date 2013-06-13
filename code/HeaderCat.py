import os, sys, glob

inFilePattern = sys.argv[1]
outFilePath = sys.argv[2]

headerWritten = False
outFile = open(outFilePath, 'w')

for inFilePath in glob.glob(inFilePattern):
    inFile = open(inFilePath)

    headerLine = inFile.readline()

    if not headerWritten:
        outFile.write(headerLine)
        headerWritten = True

    for line in inFile:
        outFile.write(line)

    inFile.close()

outFile.close()

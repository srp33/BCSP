import os, sys, glob
import utilities

inFilePath = sys.argv[1]
columnNames = [x for x in sys.argv[2].split(",")]
outFilePath = sys.argv[3]

inFile = open(inFilePath)
outFile = open(outFilePath, 'w')

headerItems = inFile.readline().rstrip().split("\t")

columnIndices = []
for columnName in columnNames:
    if not columnName in headerItems:
        print "Column %s is not specified in the file header" % columnName
    else:
        columnIndices.append(headerItems.index(columnName))

lineCount = 0
out = ""

for line in inFile:
    lineCount += 1
    lineItems = line.rstrip().split("\t")
    outItems = [lineItems[i] for i in columnIndices]

    out += "\t".join(outItems) + "\n"

    if lineCount % 100000 == 0:
        print lineCount
        outFile.write(out)
        out = ""

outFile.write(out)
outFile.close()
inFile.close()

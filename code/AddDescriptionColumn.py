import os, sys, glob
import utilities

inFilePath = sys.argv[1]
keyDescriptionFilePath = sys.argv[2]
default = sys.argv[3]

data = utilities.readMatrixFromFile(inFilePath)

keyDescriptionDict = {}
for line in file(keyDescriptionFilePath):
    lineItems = line.rstrip().split("\t")
    keyDescriptionDict[lineItems[0]] = lineItems[1]

modData = []
for row in data:
    description = default

    if keyDescriptionDict.has_key(row[0]):
        description = keyDescriptionDict[row[0]]

    modData.append(row + [description])

outFile = open(inFilePath, 'w')
for row in modData:
    outFile.write("\t".join(row) + "\n")
outFile.close()

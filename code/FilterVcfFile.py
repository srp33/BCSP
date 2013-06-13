import os, sys, glob
from utilities import *
from VariantUtilities import *

inFilePath = sys.argv[1]
positionIDFilePath = sys.argv[2]
outFilePath = sys.argv[3]

positionIDs = set(readVectorFromFile(positionIDFilePath))

outFile = open(outFilePath, 'w')

count = 0
numKept = 0
for line in file(inFilePath):
    if line.startswith("#"):
        outFile.write(line)
        continue

    count += 1
    if count % 100000 == 0:
        print count

    lineItems = line.rstrip().split("\t")
    positionID = parseChromosome(lineItems[0]) + "__" + lineItems[1]

    if positionID in positionIDs:
        continue

    outFile.write(line)
    numKept += 1

outFile.close()

print "Stats for %s -- %s:" % (inFilePath, positionIDFilePath)
print "Input: %i variants" % count
print "Output: %i variants" % numKept

import os, sys, glob
import utilities

inDirPath = sys.argv[1]
metric = sys.argv[2]
outFilePath = sys.argv[3]

values = []

for inFilePath in utilities.globRecursive(inDirPath, "*Results.txt"):
    for line in file(inFilePath):
        lineItems = line.rstrip().split("\t")

        if lineItems[0] == metric:
            values.append(float(lineItems[1]))

utilities.writeVectorToFile(values, outFilePath)

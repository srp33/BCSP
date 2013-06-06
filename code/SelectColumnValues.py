import os, sys, glob
import utilities

inFilePath = sys.argv[1]
valueColumnIndex = int(sys.argv[2])
queryColumnIndex = int(sys.argv[3])
queryValues = sys.argv[4].split(",")
outFilePath = sys.argv[5]

data = utilities.readMatrixFromFile(inFilePath)
data = [x for x in data if x[0][0] != "#"]

outFile = open(outFilePath, 'w')
for row in data:
    if row[queryColumnIndex] in queryValues:
        outFile.write(row[valueColumnIndex] + "\n")
outFile.close()

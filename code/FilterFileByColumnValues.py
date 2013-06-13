import os, sys, glob
import utilities

inFilePath = sys.argv[1]
filterColumnIndex = int(sys.argv[2])
filterValuesFilePathOrValue = sys.argv[3]
numHeaderRows = int(sys.argv[4])
outFilePath = sys.argv[5]

negate = False
if len(sys.argv) >= 7:
    negate = sys.argv[6] == "True"

def convertMatrixToText(matrix):
    output = ""

    for row in matrix:
        output += "\t".join(row) + "\n"

    return output

def filterRows(rows):
    if negate:
        return [x for x in rows if x[filterColumnIndex] not in filterValues]
    else:
        return [x for x in rows if x[filterColumnIndex] in filterValues]

if os.path.exists(filterValuesFilePathOrValue):
    filterValues = set([x.strip() for x in file(filterValuesFilePathOrValue)])
else:
    filterValues = set(filterValuesFilePathOrValue.split(","))
    if len(filterValues) == 1:
        print "Warning: No file %s exists." % filterValues[0]

outFile = open(outFilePath, 'w')
inFile = open(inFilePath)

for i in range(numHeaderRows):
    outFile.write(inFile.readline())

lineCount = 0
data = []
for line in inFile:
    lineCount += 1
    lineItems = line.rstrip().split("\t")
    data.append(lineItems)

    if len(data) == 10000:
        outFile.write(convertMatrixToText(filterRows(data)))
        print lineCount
        data = []

if len(data) > 0:
#    for x in data:
#        print x[filterColumnIndex]
    outFile.write(convertMatrixToText(filterRows(data)))
    print lineCount

inFile.close()
outFile.close()

import os, sys, glob

inFilePath = sys.argv[1]
groupSize = int(sys.argv[2])
outDirPath = sys.argv[3]

def saveOutput(outLines, groupCount):
    outFilePath = outDirPath + "/" + os.path.basename(inFilePath).replace(".xml", "_%i.xml" % groupCount)
    outFile = open(outFilePath, 'w')
    outFile.write(headerLine1)
    outFile.write(headerLine2)
    for line in outLines:
        outFile.write(line)
    outFile.write(footerLine)
    outFile.close()

inFileLines = [line for line in file(inFilePath)]

headerLine1 = inFileLines.pop(0)
headerLine2 = inFileLines.pop(0)

if "Query" in inFileLines[0]:
    inFileLines.pop(0)
    inFileLines.pop(0)

footerLine = inFileLines.pop(len(inFileLines)-1)

groupCount = 1
resultCount = 0
outLines = []

for line in inFileLines:
    if "<Result" in line:
        if resultCount == groupSize:
            saveOutput(outLines, groupCount)
            print "Done processing result group %i (%i samples)" % (groupCount, resultCount)
            groupCount += 1
            resultCount = 0
            outLines = []

        resultCount += 1

    outLines.append(line)

if len(outLines) > 0:
    saveOutput(outLines, groupCount)
    print "Done processing result group %i (%i samples)" % (groupCount, resultCount)

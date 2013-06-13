import os, sys
import utilities

filePath = sys.argv[1]

outLines = []
for line in file(filePath):
    if "\r\n" in line:
        for line2 in line.split("\r\n"):
            outLines.append(line2)
    elif "\r" in line:
        for line2 in line.split("\r"):
            outLines.append(line2)
    else:
        outLines.append(line)

outFile = open(filePath, 'w')
for line in outLines:
    outFile.write(line.rstrip() + "\n")
outFile.close()

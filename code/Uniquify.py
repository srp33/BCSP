import os, sys, glob

filePath = sys.argv[1]

values = [line.rstrip() for line in file(filePath)]
values = list(set(values))

outFile = open(filePath, 'w')
outFile.write("\n".join(values))
outFile.close()

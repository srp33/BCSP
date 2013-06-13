import os, sys, glob
import utilities

inFilePath = sys.argv[1]
startingWith = sys.argv[2]

for line in file(inFilePath):
    if line.startswith(startingWith):
        print line.rstrip()

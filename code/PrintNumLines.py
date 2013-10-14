import os, sys, glob

inFilePath = sys.argv[1]

count = 0
for line in file(inFilePath):
    count += 1

print count

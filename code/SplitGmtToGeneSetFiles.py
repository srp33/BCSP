import os, sys, glob
import utilities

gmtFilePath = sys.argv[1]
ignorePattern = sys.argv[2]
outFilePattern = sys.argv[3]

for line in file(gmtFilePath):
    lineItems = line.rstrip().split("\t")
    name = lineItems[0]
    genes = lineItems[2:]

    if ignorePattern in name:
        print "Ignoring %s" % name
        continue

    outFilePath = outFilePattern.replace("{PATHWAY_NAME}", name)
    print "Saving to %s" % outFilePath
    utilities.writeVectorToFile(genes, outFilePath)

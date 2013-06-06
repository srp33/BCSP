import os, sys, glob

inFilePath = sys.argv[1]
includeSnps = sys.argv[2] == "True"
includeExonsOnly = sys.argv[3] == "True"
outFilePath = sys.argv[4]

inFile = open(inFilePath)
headerItems = inFile.readline().rstrip().split("\t")

probes = []

for line in inFile:
    lineItems = line.rstrip().split("\t")
    probe = lineItems[headerItems.index("pr_text")]
    hasSnp = lineItems[headerItems.index("SNP")] != "0"
    junctionType = lineItems[headerItems.index("junction type")]

    snpYes = False
    junctionTypeYes = False

    if includeSnps:
        snpYes = True
    else:
        if not hasSnp:
            snpYes = True

    if includeExonsOnly:
        if junctionType == "exon":
            junctionTypeYes = True
    else:
        junctionTypeYes = True

    if snpYes and junctionTypeYes:
        probes.append(probe)

inFile.close()
print len(probes)

outFile = open(outFilePath, 'w')
outFile.write("\n".join(probes))
outFile.close()

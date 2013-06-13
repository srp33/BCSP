import os, sys, glob
from operator import itemgetter, attrgetter
import utilities

inFilePath = sys.argv[1]
outPositionsFilePath = sys.argv[2]
outExonBedFilePath = sys.argv[3]
outStrandFilePath = sys.argv[4]

# Get all entries associated with each gene
geneDict = {}
for line in file(inFilePath):
    lineItems = line.rstrip().split("\t")
    lineItems[2] = int(lineItems[2]) + 1
    lineItems[3] = int(lineItems[3]) + 1
    if "|" not in lineItems[1]:
        geneDict[lineItems[10]] = geneDict.setdefault(lineItems[10], []) + [lineItems]

outPositionsFile = open(outPositionsFilePath, 'w')
outStrandFile = open(outStrandFilePath, 'w')
outExonBedRows = []

for gene in sorted(geneDict.keys()):
    geneInfo = [x for x in geneDict[gene] if x[11] == "GENE"]

    rnaInfo = [x for x in geneDict[gene] if x[11] == "RNA" and len(x) > 14 and "best" in x[14]]
    if len(rnaInfo) == 0:
        continue
    rnaID = rnaInfo[0][13]

    ##geneInfoForBestRna = [x for x in geneDict[gene] if x[13] == rnaID and x[11] in ("CDS", "UTR")]
    ##size = 0
    ##for x in geneInfoForBestRna:
    ##    size += int(x[3]) - int(x[2]) + 1
    ##    print x
    ##print size
    ##exit()

    transcribedInfo = [x for x in geneDict[gene] if x[11] in ("UTR", "CDS") and x[13] == rnaID]
    cdsInfo = [x for x in geneDict[gene] if x[11] == "CDS" and x[13] == rnaID]

    if len(geneInfo) == 1:
        geneInfoSingle = geneInfo[0]
    else:
        if geneInfo[0][1] in ("X", "Y"):
            continue

        geneInfoSingle = list(geneInfo[0])
        geneInfoSingle[2] = int("%i" % min([x[2] for x in geneInfo]))
        geneInfoSingle[3] = int("%i" % max([x[3] for x in geneInfo]))

        if (int(geneInfoSingle[3]) - int(geneInfoSingle[2])) > 10000000:
            print "Gene too long, thus ignoring it: %s" % geneInfoSingle[10]

    geneID = geneInfoSingle[10].replace("GeneID:", "")
    chromosome = "chr" + geneInfoSingle[1]
    outPositionsRow = [geneID, chromosome, geneInfoSingle[2], geneInfoSingle[3]]

    # Add exon positions
    if len(transcribedInfo) == 0:
        outPositionsRow.append(geneInfoSingle[2])
        outPositionsRow.append(geneInfoSingle[3])

        outExonBedRows.append([chromosome, geneInfoSingle[2], geneInfoSingle[3], geneID])
    else:
        outPositionsRow.append(",".join([str(x[2]) for x in transcribedInfo]))
        outPositionsRow.append(",".join([str(x[3]) for x in transcribedInfo]))

        for i in range(len(transcribedInfo)):
            outExonBedRows.append([chromosome, str(transcribedInfo[i][2]), str(transcribedInfo[i][3]), "%s_%i" % (geneID, i)])

    # Add transcription start site
    if len(cdsInfo) == 0:
        if len(transcribedInfo) > 0:
            outPositionsRow.append(",".join([str(x[2]) for x in transcribedInfo]))
            outPositionsRow.append(",".join([str(x[3]) for x in transcribedInfo]))
        else:
            outPositionsRow.append(geneInfoSingle[2])
            outPositionsRow.append(geneInfoSingle[3])
    else:
        outPositionsRow.append(",".join([str(x[2]) for x in cdsInfo]))
        outPositionsRow.append(",".join([str(x[3]) for x in cdsInfo]))

    utilities.writeMatrixToOpenFile([outPositionsRow], outPositionsFile)
    utilities.writeMatrixToOpenFile([[geneID, geneInfoSingle[4]]], outStrandFile)

utilities.writeMatrixToFile(outExonBedRows, outExonBedFilePath)

outPositionsFile.close()
outStrandFile.close()

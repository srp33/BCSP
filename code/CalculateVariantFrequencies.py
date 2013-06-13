import os, sys, glob, shutil, collections, random
from utilities import *
from VariantUtilities import *
from operator import itemgetter, attrgetter

vcfFilePatterns = sys.argv[1].split(",")
outFilePath = sys.argv[2]
outSummaryFilePath = sys.argv[3]

if os.path.exists(outSummaryFilePath):
    os.remove(outSummaryFilePath)

positionFrequencyDict = {}
uniqueSampleIDs = set()

vcfFilePaths = []
for vcfFilePattern in vcfFilePatterns:
    vcfFilePaths.extend(glob.glob(vcfFilePattern))

for vcfFilePath in vcfFilePaths:
    printFlush("Loading data from %s" % vcfFilePath, outSummaryFilePath)
    vcfFile = open(vcfFilePath)
    line = vcfFile.readline()
    while line.startswith("##"):
        line = vcfFile.readline()

    sampleIDs = line.rstrip().split("\t")[9:]
    for sampleID in sampleIDs:
        uniqueSampleIDs.add(sampleID)

    lineCount = 0
    for line in vcfFile:
        lineCount += 1
        lineItems = line.rstrip().split("\t")

        parseResult = parseVcfLine(line, sampleIDs, None)
        if parseResult == None:
            continue
        positionID, chromosome, position, externalID, ref, variantType, genotypeValuesList, geneAnnotationDict, variantAnnotationDict = parseResult

        if lineCount % 10000 == 0:
            printFlush("Parsing variant info for %s - %i" % (chromosome, position))

        if positionID not in positionFrequencyDict:
            positionFrequencyDict[positionID] = {"HC": 0, "HT": 0, "HR": 0, "UK": 0}

        for i in range(len(genotypeValuesList)):
            description = genotypeValuesList[i][1]
            positionFrequencyDict[positionID][description] += 1

    vcfFile.close()

printFlush("%i unique variant positions" % len(positionFrequencyDict), outSummaryFilePath)
printFlush("%i unique sample IDs" % len(uniqueSampleIDs), outSummaryFilePath)

outFile = open(outFilePath, 'w')

for positionID in sorted(positionFrequencyDict):
    minorAlleleDescription = getMinorAlleleDescription(positionFrequencyDict[positionID])
    majorAlleleDescription = ["HC", "HR"][minorAlleleDescription == "HC"]

    numSamplesWithData = sum(positionFrequencyDict[positionID].values())
    positionFrequencyDict[positionID][majorAlleleDescription] += (len(uniqueSampleIDs) - numSamplesWithData)

    maf = calculateNonRefAlleleFrequency(positionFrequencyDict[positionID])

    writeMatrixToOpenFile([["%s__%i" % (positionID[0], positionID[1]), maf]], outFile)

outFile.close()

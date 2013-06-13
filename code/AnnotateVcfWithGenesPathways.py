import os, sys, glob, shutil, collections, random
from utilities import *
from VariantUtilities import *
from operator import itemgetter, attrgetter

inVcfFilePath = sys.argv[1]
genePositionsFilePath = sys.argv[2]
geneSymbolsFilePath = sys.argv[3]
geneStrandFilePath = sys.argv[4]
pathwayGmtFilePaths = sys.argv[5].split(",")
outVariantsInExonsFilePath = sys.argv[6]
outVariantsNotInExonsFilePath = sys.argv[7]
outVcfFilePath = sys.argv[8]

genePathwayDict, pathwayGeneDict = parsePathwayInfo(pathwayGmtFilePaths)
positionGeneDict = buildPositionGeneExonDict(genePositionsFilePath)
geneSymbolsDict = buildGeneSymbolsDict(geneSymbolsFilePath)
geneStrandDict = buildGeneStrandDict(geneStrandFilePath)

outVcfFile = open(outVcfFilePath, 'w')

if outVariantsInExonsFilePath != "None":
    positionsInExonsOutFile = open(outVariantsInExonsFilePath, 'w')
    positionsNotInExonsOutFile = open(outVariantsNotInExonsFilePath, 'w')
    writeMatrixToOpenFile([["track name=%s" % os.path.basename(outVariantsInExonsFilePath)]], positionsInExonsOutFile)
    writeMatrixToOpenFile([["track name=%s" % os.path.basename(outVariantsNotInExonsFilePath)]], positionsNotInExonsOutFile)

genesWithVariants = set()

for line in file(inVcfFilePath):
    if line.startswith("#"):
        outVcfFile.write(line)
        continue

    lineItems = line.rstrip().split("\t")

    chromosome = parseChromosome(lineItems[0])
    position = int(lineItems[1])

    if chromosome in positionGeneDict and position in positionGeneDict[chromosome]:
        geneID = positionGeneDict[chromosome][position]
        symbol = getGeneSymbol(geneSymbolsDict, geneID)
        strand = geneStrandDict[geneID]

        lineItems[7] += ";GeneID=%i;GeneSymbol=%s;Strand=%s" % (geneID, symbol, strand)

        pathways = []
        if geneID in genePathwayDict:
            pathways = genePathwayDict[geneID]

        if len(pathways) > 0:
            lineItems[7] += ";Pathways=%s" % ",".join(pathways)

        writeMatrixToOpenFile([lineItems], outVcfFile)

        if outVariantsInExonsFilePath != "None":
            writeMatrixToOpenFile([[chromosome, position, position, symbol]], positionsInExonsOutFile)

        genesWithVariants.add(geneID)
    else:
        if outVariantsInExonsFilePath != "None":
            writeMatrixToOpenFile([[chromosome, position, position, ""]], positionsNotInExonsOutFile)
        continue

if outVariantsInExonsFilePath != "None":
    positionsInExonsOutFile.close()
    positionsNotInExonsOutFile.close()

outVcfFile.close()

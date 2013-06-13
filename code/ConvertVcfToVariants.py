import os, sys, glob, shutil, collections, random
from utilities import *
from VariantUtilities import *

annotatedVcfFilePatterns = sys.argv[1].split(",")
acceptedImpactLevels = set(sys.argv[2].split(","))
outVariantsFilePath = sys.argv[3]
outSummaryFilePath = sys.argv[4]

def parseVariants():
    annotatedVcfFilePaths = []
    for annotatedVcfFilePattern in annotatedVcfFilePatterns:
        annotatedVcfFilePaths.extend(glob.glob(annotatedVcfFilePattern))

    positionVariantDict = {}
    allSampleIDs = set()

    for annotatedVcfFilePath in annotatedVcfFilePaths:
        printFlush("Loading data from %s" % annotatedVcfFilePath, outSummaryFilePath)
        annotatedVcfFile = open(annotatedVcfFilePath)
        line = annotatedVcfFile.readline()
        while line.startswith("##"):
            line = annotatedVcfFile.readline()

        sampleIDs = line.rstrip().split("\t")[9:]
        for x in sampleIDs:
            allSampleIDs.add(x)

        lineCount = 0
        for line in annotatedVcfFile:
            lineCount += 1
            if lineCount % 1000 == 0:
                print lineCount

            positionID, chromosome, position, externalID, ref, variantType, genotypeValuesList, geneAnnotationDict, variantAnnotationDict = parseVcfLine(line, sampleIDs, acceptedImpactLevels)

            if positionID not in positionVariantDict:
                positionVariantDict[positionID] = []

            for i in range(len(genotypeValuesList)):
                genotypeValues = genotypeValuesList[i]

                description = genotypeValues[1]
                alt = genotypeValues[0]

                sampleID = sampleIDs[i]

                variantDict = {}
                variantDict["Chromosome"] = chromosome
                variantDict["Position"] = position
                variantDict["SampleID"] = sampleID
                variantDict["Ref"] = ref
                variantDict["Alt"] = alt
                variantDict["ExternalID"] = externalID
                variantDict["Description"] = description
                variantDict["VariantType"] = variantType
                variantDict["VariantSubType"] = getVariantSubType(ref, alt, variantType)

                variantDict.update(geneAnnotationDict)

                for metaKey in ("VariantEffect", "VariantImpactLevel", "SIFT", "Polyphen", "MutAssr", "CondelScore", "CondelCall", "GeneID", "GeneSymbol", "Strand", "Pathways"):
                    if variantAnnotationDict.has_key(alt) and variantAnnotationDict[alt].has_key(metaKey):
                        variantDict[metaKey] = variantAnnotationDict[alt][metaKey]

                positionVariantDict[positionID].append(variantDict)

        annotatedVcfFile.close()

    printStats("Before filtering", positionVariantDict, allSampleIDs)
    removeCommonAllele(positionVariantDict)
    printStats("After removing common allele", positionVariantDict, allSampleIDs)
    removeVariantsByFunction(positionVariantDict)
    printStats("After filtering for function", positionVariantDict, allSampleIDs)
    removeNonPathogenicVariants(positionVariantDict)
    printStats("After filtering for conservation", positionVariantDict, allSampleIDs)

    saveVariantsToFile(positionVariantDict)

def removeCommonAllele(positionVariantDict):
    for position in positionVariantDict.keys():
        freqDict = {"HC": 0, "HT": 0, "HR": 0, "UK": 0}
        for variantDict in positionVariantDict[position]:
            freqDict[variantDict["Description"]] += 1

        majorAlleleDescription = getMajorAlleleDescription(freqDict)
        filterPositionByKeyValue(positionVariantDict, position, "Description", majorAlleleDescription)

def removeVariantsByFunction(positionVariantDict):
    for positionID in positionVariantDict.keys():
        filterPositionByKeyValue(positionVariantDict, positionID, "VariantEffect", None)

def removeNonPathogenicVariants(positionVariantDict):
    for positionID in positionVariantDict.keys():
        variantDictList = []
        for variantDict in positionVariantDict.pop(positionID):
            if ("VariantImpactLevel" in variantDict and variantDict["VariantImpactLevel"] == "HIGH") or variantDict["VariantType"] == "INDEL" or ("CondelCall" in variantDict and variantDict["CondelCall"] == "deleterious"):
                variantDictList.append(variantDict)

        if len(variantDictList) > 0:
            positionVariantDict[positionID] = variantDictList

def saveVariantsToFile(positionVariantDict):
    keys = [key for key in sorted(positionVariantDict[positionVariantDict.keys()[0]][0])]

    outData = [keys]
    for positionID in sorted(positionVariantDict):
        for variantDict in positionVariantDict[positionID]:
            outData.append([variantDict[key] for key in keys])

    writeMatrixToFile(outData, outVariantsFilePath)

def printStats(description, positionVariantDict, sampleIDs):
    snvPositions = [x for x in positionVariantDict.keys() if x[2] == "SNV"]
    indelPositions = [x for x in positionVariantDict.keys() if x[2] == "INDEL"]
    numSnv = float(sum([len(positionVariantDict[x]) for x in snvPositions]))
    numIndel = float(sum([len(positionVariantDict[x]) for x in indelPositions]))

    printFlush(description, outSummaryFilePath)
    printFlush("Total variants: %i" % (numSnv + numIndel), outSummaryFilePath)
    printFlush("Total positions: %i" % len(positionVariantDict), outSummaryFilePath)
    printFlush("SNV total: %i" % numSnv, outSummaryFilePath)
    printFlush("SNV positions: %i" % len(snvPositions), outSummaryFilePath)
    printFlush("SNVs per sample: %.3f" % (numSnv / float(len(sampleIDs))), outSummaryFilePath)
    printFlush("Indel total: %i" % numIndel, outSummaryFilePath)
    printFlush("Indel positions: %i" % len(indelPositions), outSummaryFilePath)
    printFlush("Indels per sample: %.3f" % (numIndel / float(len(sampleIDs))), outSummaryFilePath)

if os.path.exists(outSummaryFilePath):
    os.remove(outSummaryFilePath)

parseVariants()

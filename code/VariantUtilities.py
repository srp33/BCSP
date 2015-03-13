import os, sys, glob, shutil, collections, random
from utilities import *
from operator import itemgetter, attrgetter

def buildGenePositionsDict(genePositionsFilePath):
    genePositionsDict = {}
    exonPositionsDict = {}
    transcribedPositionsDict = {}

    for line in file(genePositionsFilePath):
        lineItems = line.rstrip().split("\t")
        geneID = int(lineItems[0])
        chromosome = lineItems[1]
        start = int(lineItems[2])
        stop = int(lineItems[3])
        exonStarts = lineItems[4].split(",")
        exonStops = lineItems[5].split(",")
        exonCoords = [(int(exonStarts[i]), int(exonStops[i])) for i in range(len(exonStarts))]
        transcribedStarts = lineItems[6].split(",")
        transcribedStops = lineItems[7].split(",")
        transcribedCoords = [(int(transcribedStarts[i]), int(transcribedStops[i])) for i in range(len(transcribedStarts))]

        genePositionsDict[geneID] = (chromosome, start, stop)
        exonPositionsDict[geneID] = exonCoords
        transcribedPositionsDict[geneID] = transcribedCoords

    return genePositionsDict, exonPositionsDict, transcribedPositionsDict

def buildGeneStrandDict(geneStrandFilePath):
    geneStrandDict = {}

    for line in file(geneStrandFilePath):
        lineItems = line.rstrip().split("\t")
        geneID = int(lineItems[0])
        strand = lineItems[1]
        geneStrandDict[geneID] = strand

    return geneStrandDict

def buildGeneSymbolsDict(geneSymbolsFilePath):
    geneSymbolsDict = {}

    for line in file(geneSymbolsFilePath):
        lineItems = line.rstrip().split("\t")
        geneSymbolsDict[int(lineItems[0])] = lineItems[1]

    return geneSymbolsDict

def getGeneSymbol(geneSymbolsDict, geneID):
    geneSymbol = "[No Symbol]"
    if geneID in geneSymbolsDict:
        geneSymbol = geneSymbolsDict[geneID]

    return geneSymbol

def buildGeneSequencesDict(geneSequencesFilePath):
    geneSequencesDict = {}

    for line in file(geneSequencesFilePath):
        lineItems = line.rstrip().split("\t")
        geneSequencesDict[int(lineItems[0])] = lineItems[1]

    return geneSequencesDict

def parsePathwayInfo(pathwayGmtFilePaths, geneIDsToConsider=[]):
    genePathwayDict = {}
    pathwayGeneDict = {}

    geneIDsToConsider2 = set([int(x) for x in geneIDsToConsider])

    for pathwayGmtFilePath in pathwayGmtFilePaths:
        for gmtRow in readMatrixFromFile(pathwayGmtFilePath):
            pathwayName = gmtRow[0]
            geneIDs = [int(x) for x in gmtRow[2:]]

            for geneID in geneIDs:
                if len(geneIDsToConsider2) == 0 or geneID in geneIDsToConsider2:
                    genePathwayDict[geneID] = genePathwayDict.setdefault(geneID, []) + [pathwayName]
                    pathwayGeneDict[pathwayName] = pathwayGeneDict.setdefault(pathwayName, []) + [geneID]

    return genePathwayDict, pathwayGeneDict

def getSamples(sampleFilePath):
    samples = set()

    if os.path.exists(sampleFilePath):
        samples = set(readVectorFromFile(sampleFilePath))

    return samples

def buildGeneVariantDict(positionVariantDict):
    geneVariantDict = {}

    for positionID in positionVariantDict:
        for variantDict in positionVariantDict[positionID]:
            geneVariantDict[variantDict["GeneID"]] = geneVariantDict.setdefault(variantDict["GeneID"], []) + [variantDict]

    return geneVariantDict

def filterPositionByKeyValue(positionVariantDict, positionID, key, value):
    variantDictList = []
    for variantDict in positionVariantDict.pop(positionID):
        if variantDict.has_key(key) and variantDict[key] != value:
            variantDictList.append(variantDict)

    if len(variantDictList) > 0:
        positionVariantDict[positionID] = variantDictList

def removeUnknownVariants(positionVariantDict):
    for positionID in positionVariantDict.keys():
        filterPositionByKeyValue(positionVariantDict, positionID, "Description", "UK")

def readVariants(filePath):
    variantData = readMatrixFromFile(filePath)
    variantHeaderItems = variantData.pop(0)

    variantList = []
    for row in variantData:
        variantDict = {}
        for i in range(len(variantHeaderItems)):
            variantDict[variantHeaderItems[i]] = row[i]

        variantList.append(variantDict)

    return variantList

def buildGroupVariantDict(variantList, groupVariable):
    groupDict = {}

    for groupValue in set([x[groupVariable] for x in variantList]):
        for x in groupValue.split(","):
            groupDict[x] = []

    for variantDict in variantList:
        for x in variantDict[groupVariable].split(","):
            groupDict[x].append(variantDict)

    return groupDict

def getUniqueValues(queryVariantList, key):
    values = set()
    for variantDict in queryVariantList:
        for value in variantDict[key].split(","):
            if value != "":
                values.add(value)

    return sorted(list(values))

def filterVariants(variantList, key, values, negate=False):
    valueSet = set(values)

    if negate:
        return [variantDict for variantDict in variantList if len(valueSet & set(variantDict[key].split(","))) == 0]
    else:
        return [variantDict for variantDict in variantList if len(valueSet & set(variantDict[key].split(","))) > 0]

def removeVariantsWithFewVariantsPerGene(variantList, minVariantsPerGene):
    geneVariantDict = buildGroupVariantDict(variantList, "GeneID")
    filteredVariantList = []

    for geneID in geneVariantDict.keys():
        if len(geneVariantDict[geneID]) < minVariantsPerGene:
            del geneVariantDict[geneID]
        else:
            filteredVariantList.extend(geneVariantDict[geneID])

    return filteredVariantList

def buildPositionGeneExonDict(genePositionsFilePath):
    printFlush("Building position-gene-exon dictionary")
    genePositionsDict, exonPositionsDict, transcribedPositionsDict = buildGenePositionsDict(genePositionsFilePath)

    positionGeneDict = {}

    for geneID in sorted(genePositionsDict.keys()):
        geneChromosome, geneStart, geneStop = genePositionsDict[geneID]
        positionGeneDict.setdefault(geneChromosome, {})

        for exonPositions in exonPositionsDict[geneID]:
            for i in range(exonPositions[0] - 2, exonPositions[1] + 3):
                positionGeneDict[geneChromosome][i] = geneID

    return positionGeneDict

def writePositionGeneExonDict(positionGeneDict, outFilePath):
    outFile = open(outFilePath, 'w')

    for chromosome in positionGeneDict:
        for position in positionGeneDict[chromosome]:
            outFile.write("%s\t%i\t%i\n" % (chromosome, position, positionGeneDict[chromosome][position]))

    outFile.close()

def parseGenotypeValues(genotypeKeys, genotypeValues, alleles):
    def getValue(key, defaultValue):
        if key in genotypeKeys:
            index = genotypeKeys.index(key)

            if index < len(genotypeValues):
                return genotypeValues[index]
        return defaultValue

    if genotypeValues[0] == "./.":
        return ["Unknown", "UK", -1.0, -1]

    if "PQ" in genotypeKeys and len(genotypeValues) != len(genotypeKeys):
        del genotypeKeys[genotypeKeys.index("PQ")]

    genotype = getValue("GT", "Unknown").replace("|", "/")
    description = genotype.replace("|", "/").replace("2", "1").replace("3", "1").replace("4", "1").replace("5", "1").replace("6", "1").replace("0/1", "HT").replace("1/0", "HT").replace("1/1", "HR").replace("0/0", "HC")

    altIndex = max([int(x) for x in genotype.split("/")])
    alt = alleles[altIndex]

    return [alt, description, float(getValue("GQ", -1.0)), int(getValue("DP", -1))]

def parseVcfLine(line, sampleIDs, acceptedImpactLevels):
    lineItems = line.rstrip().split("\t")

    if lineItems[6] != "PASS":
        return

    chromosome = parseChromosome(lineItems[0])

#    if chromosome != "chr22":
#        return

    position = int(lineItems[1])
    externalID = lineItems[2]
    ref = lineItems[3]
    altOptions = lineItems[4].split(",")
    alleles = [ref] + altOptions
    variantType = getVariantType(alleles)
    positionID = (chromosome, position, variantType)
    genotypeValuesList = [parseGenotypeValues(lineItems[8].split(":"), genotypeRaw.split(":"), alleles) for genotypeRaw in lineItems[9:]]

    geneAnnotationDict = {}
    for key in ("GeneID", "GeneSymbol", "Strand", "Pathways"):
        geneAnnotationDict[key] = parseMetaValue(lineItems[7], key)

    # Only parse if necessary
    variantAnnotationDict = None
    if acceptedImpactLevels != None:
        variantAnnotationDict = parseVariantAnnotations(ref, altOptions, lineItems[7], acceptedImpactLevels)

    return positionID, chromosome, position, externalID, ref, variantType, genotypeValuesList, geneAnnotationDict, variantAnnotationDict

def parseVariantAnnotations(ref, altOptions, rawMeta, acceptedImpactLevels):
    snpEffItems = parseSnpEffItems(rawMeta, acceptedImpactLevels)

    if len(snpEffItems) == 0:
        return {}

    metaDict = {}

    if len(altOptions) == 1 or len(snpEffItems) == 1:
        for alt in altOptions:
            metaDict[alt] = summarizeMetaItems(alt, rawMeta, snpEffItems)
    else:
        snpEffItemsDiffDict = {}
        for snpEffItem in snpEffItems:
            if snpEffItem[2] == "-":
                snpEffDiff = 0
            else:
                snpEffRef = snpEffItem[2].split("/")[0].replace("-", "")
                snpEffAlt = snpEffItem[2].split("/")
                if len(snpEffAlt) > 1:
                    snpEffAlt = snpEffAlt[1].replace("-", "")
                else:
                    snpEffAlt = snpEffRef
                snpEffDiff = len(snpEffAlt) - len(snpEffRef)

            snpEffItemsDiffDict[snpEffDiff] = snpEffItemsDiffDict.setdefault(snpEffDiff, []) + [snpEffItem]

        for alt in altOptions:
            altDiff = len(alt) - len(ref)

            if altDiff in snpEffItemsDiffDict:
                altSnpEffItems = snpEffItemsDiffDict[altDiff]
            else:
                altSnpEffItems = snpEffItemsDiffDict[0]

            metaDict[alt] = summarizeMetaItems(alt, rawMeta, altSnpEffItems)

    return metaDict

def getVariantType(alleles):
    return ("SNV", "INDEL")[int(len([x for x in alleles if len(x) > 1]) > 0)]

def getVariantSubType(ref, alt, variantType):
    if variantType == "SNV":
        return "%s>%s" % (ref, alt)

    if len(ref) > len(alt):
        return "DEL"

    return "INS"

def parseSnpEffItems(rawMeta, acceptedImpactLevels):
    if ";EFF=" not in rawMeta:
        return []

    snpEffRawItems = [y for y in rawMeta.split(';') if y.startswith('EFF=')][0].replace('EFF=', '').split("),")
    snpEffItems = set()

    for snpEffRawItem in snpEffRawItems:
        effect = snpEffRawItem.split("(")[0]
        metaItems = snpEffRawItem.split("(")[1].split("|")

        impactLevel = metaItems[0]
        if acceptedImpactLevels != None and impactLevel not in acceptedImpactLevels:
            continue

        codingChange = metaItems[2]

        snpEffItems.add((effect, impactLevel, codingChange))

    return snpEffItems

def summarizeMetaItems(alt, rawMeta, snpEffItems):
    effect = ",".join(sorted(list(set([x[0] for x in snpEffItems]))))
    impactLevel = ",".join(sorted(list(set([x[1] for x in snpEffItems]))))
    sift = parsePathogenicityValue(rawMeta, "SIFT", alt)
    polyphen = parsePathogenicityValue(rawMeta, "Polyphen", alt)
    mutassr = parsePathogenicityValue(rawMeta, "MutAssr", alt)
    condelScore = parsePathogenicityValue(rawMeta, "CondelScore", alt)
    condelCall = parsePathogenicityValue(rawMeta, "CondelCall", alt)

    return {"VariantEffect": effect, "VariantImpactLevel": impactLevel, "SIFT": sift, "Polyphen": polyphen, "MutAssr": mutassr, "CondelScore": condelScore, "CondelCall": condelCall}

def parsePathogenicityValue(rawMeta, key, alt):
    items = [y for y in rawMeta.split(';') if y.startswith("%s" % key)]

    for item in rawMeta.split(";"):
        if item.startswith("%s" % key) and alt == item.split("=")[0].split("__")[1]:
            return item.split("=")[1]

    return ""

def getPathogenicityValue(dictionary, alt):
    if alt in dictionary:
        return dictionary[alt]
    return ""

def parseMetaValue(rawMeta, queryKey):
    for rawMetaValue in rawMeta.split(";"):
        key = rawMetaValue.split("=")[0]

        if key == queryKey:
            return rawMetaValue.split("=")[1]

    return ""

def getMinorAlleleDescription(freqDict):
    if (freqDict["HR"] + freqDict["HT"]) > (freqDict["HC"] + freqDict["HT"]):
        return "HC"
    return "HR"

def getMajorAlleleDescription(freqDict):
    if (freqDict["HR"] + freqDict["HT"]) <= (freqDict["HC"] + freqDict["HT"]):
        return "HC"
    return "HR"

def calculateNonRefAlleleFrequency(frequencyDict):
    uk = float(frequencyDict["UK"])
    hr = float(frequencyDict["HR"])
    ht = float(frequencyDict["HT"])
    hc = float(frequencyDict["HC"])
    allKnown = hc + ht + hr

    return min([(ht + hr) / allKnown, (hc + ht) / allKnown])

def parseChromosome(chromosome):
    if not chromosome.startswith("chr"):
        chromosome = "chr" + chromosome

    return chromosome

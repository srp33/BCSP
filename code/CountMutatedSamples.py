import os, sys, glob, shutil, collections, random
from utilities import *
from VariantUtilities import *
from operator import itemgetter, attrgetter

variantFilePath = sys.argv[1]
allGeneIDsFilePath = sys.argv[2]
geneIDsForPathwayAnalysisFilePath = sys.argv[3]
pathwayGmtFilePaths = sys.argv[4].split(",")
entrezGeneSymbolsFilePath = sys.argv[5]
sampleFilePath = sys.argv[6]
sampleDescription = sys.argv[7]
geneIDsToExcludeFromPathways = set(sys.argv[8].split(","))
outGeneMutFreqReportFilePath = sys.argv[9]
outPathwayMutFreqReportFilePath = sys.argv[10]

def writeMutationFrequencyFiles():
    outData = [["GeneID", "GeneSymbol", "%s_Mutated" % sampleDescription, "%s_NumNonMuts" % sampleDescription, "%s_NumMuts" % sampleDescription, "%s_MutationRate" % sampleDescription]]

    samplesOfInterest = set(sampleIDs)
    if os.path.exists(sampleFilePath):
        samplesOfInterest = getSamples(sampleFilePath)

    printFlush("Retaining only samples of interest")
    queryVariantList = filterVariants(allVariantList, "SampleID", samplesOfInterest)

    printFlush("Building gene-variant dictionary")
    geneVariantDict = buildGroupVariantDict(queryVariantList, "GeneID")

    for geneID in allGeneIDs:
        if geneID in geneVariantDict:
            muts, nonMuts, mutationRate = getSamplesMutatedOrNot(geneVariantDict[geneID], samplesOfInterest)
            #geneSymbol = geneVariantDict[geneID][0]["GeneSymbol"]
        else:
            muts = []
            nonMuts = list(samplesOfInterest)
            mutationRate = 0.0
            #geneSymbol = ""

        geneSymbol = getGeneSymbol(geneSymbolsDict, int(geneID))
        outData.append([geneID, geneSymbol, ",".join(muts), len(nonMuts), len(muts), mutationRate])

    sortMatrix(outData, 4, True)
    writeMatrixToFile(outData, outGeneMutFreqReportFilePath)

    outData = [["Pathway", "PathwayMutatedGenes", "%s_Mutated" % sampleDescription, "%s_NumNonMuts" % sampleDescription, "%s_NumMuts" % sampleDescription, "%s_MutationRate" % sampleDescription]]

    printFlush("Filtering genes for pathway-based analysis")
    queryVariantList = filterVariants(pathwayVariantList, "SampleID", samplesOfInterest)
    queryVariantList = filterVariants(queryVariantList, "GeneID", geneIDsToExcludeFromPathways, negate=True)

    printFlush("Building pathway-variant dictionary")
    pathwayVariantDict = buildGroupVariantDict(queryVariantList, "Pathways")

    printFlush("Iterating through %i pathways" % len(pathwayGeneDict))
    for pathway in sorted(pathwayGeneDict.keys()):
        if pathway in pathwayVariantDict:
            muts, nonMuts, mutationRate = getSamplesMutatedOrNot(pathwayVariantDict[pathway], samplesOfInterest)

            geneSymbolVariantDict = buildGroupVariantDict(pathwayVariantDict[pathway], "GeneSymbol")
            pathwayMutatedGenes = []
            for geneSymbol in sorted(geneSymbolVariantDict.keys()):
                pathwayMutatedGenes.append("%s(%i)" % (geneSymbol, len(set([x["SampleID"] for x in geneSymbolVariantDict[geneSymbol]]))))
        else:
            muts = []
            nonMuts = list(samplesOfInterest)
            mutationRate = 0.0
            pathwayMutatedGenes = []

        outData.append([pathway, ",".join(pathwayMutatedGenes), ",".join(muts), len(nonMuts), len(muts), mutationRate])

    sortMatrix(outData, 4, True)
    writeMatrixToFile(outData, outPathwayMutFreqReportFilePath)

def getSamplesMutatedOrNot(variantList, samplesOfInterest):
    knownVariantList = filterVariants(variantList, "Description", ["UK"], True)
    mutated = getUniqueValues(knownVariantList, "SampleID")
    nonMutated = samplesOfInterest - set(mutated)
    mutationRate = float(len(mutated)) / float(len(samplesOfInterest))

    return mutated, nonMutated, mutationRate

printFlush("Reading metadata")
allGeneIDs = sorted(readVectorFromFile(allGeneIDsFilePath))

geneIDsForPathwayAnalysis = list(allGeneIDs)
if geneIDsForPathwayAnalysisFilePath != "None":
    geneIDsForPathwayAnalysis = sorted(readVectorFromFile(geneIDsForPathwayAnalysisFilePath))

genePathwayDict, pathwayGeneDict = parsePathwayInfo(pathwayGmtFilePaths, geneIDsForPathwayAnalysis)
geneSymbolsDict = buildGeneSymbolsDict(entrezGeneSymbolsFilePath)

printFlush("Reading variants from %s" % variantFilePath)
allVariantList = filterVariants(readVariants(variantFilePath), "GeneID", allGeneIDs)
pathwayVariantList = filterVariants(readVariants(variantFilePath), "GeneID", geneIDsForPathwayAnalysis)
sampleIDs = getUniqueValues(allVariantList, "SampleID")

writeMutationFrequencyFiles()

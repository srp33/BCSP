import os, sys, glob
import utilities

inFilePath = sys.argv[1]

inFile = open(inFilePath)

headerItems = inFile.readline().rstrip().split("\t")
sampleIndex = headerItems.index("SampleID")
geneIndex = headerItems.index("GeneID")
descriptionIndex = headerItems.index("Description")
pathwaysIndex = headerItems.index("Pathways")
positionIndex = headerItems.index("Position")

data = [line.rstrip().split("\t") for line in inFile]

sampleGeneVariantDict = {}
for row in data:
    sampleID = row[sampleIndex]
    geneID = row[geneIndex]
    position = row[positionIndex]

    if sampleID not in sampleGeneVariantDict.keys():
        sampleGeneVariantDict[sampleID] = {}

    if geneID not in sampleGeneVariantDict[sampleID].keys():
        sampleGeneVariantDict[sampleID][geneID] = []

    sampleGeneVariantDict[sampleID][geneID].append(position)

uniqueSamples = set([x[sampleIndex] for x in data])
print "%i samples" % len(uniqueSamples)

uniqueGenes = set([x[geneIndex] for x in data])
print "%i genes with at least one variant" % len(uniqueGenes)

uniquePathways = set()
for row in data:
    pathways = row[pathwaysIndex].split(",")
    for pathway in pathways:
        if pathway != "":
            uniquePathways.add(pathway)
print "%i pathways with at least one variant" % len(uniquePathways)

numGenesMutatedPerSample = [float(len(set([x[geneIndex] for x in data if x[sampleIndex]==sample]))) for sample in uniqueSamples]
print "%.1f genes with variant per sample" % utilities.calculateMean(numGenesMutatedPerSample)

numSamplesMutatedPerGene = [float(len(set([x[sampleIndex] for x in data if x[geneIndex]==gene]))) for gene in uniqueGenes]
print "%.1f samples with variant per gene" % utilities.calculateMean(numSamplesMutatedPerGene)

numMutatedPerGenePerSample = []
for x in sampleGeneVariantDict.keys():
    for geneID in sampleGeneVariantDict[x]:
        numMutatedPerGenePerSample.append(float(len(sampleGeneVariantDict[x][geneID])))
print "%.3f variants per gene when sample had at least one variant in gene" % utilities.calculateMean(numMutatedPerGenePerSample)

numMutatedPerGenePerSample = [float(len(set([x[geneIndex] for x in data if x[sampleIndex]==sample]))) for sample in uniqueSamples]
print "%.1f variants per genes with variant per sample" % utilities.calculateMean(numGenesMutatedPerSample)

numSamplesPerPathway = []
for pathway in uniquePathways:
    numSamplesPerPathway.append(float(len(set([row[sampleIndex] for row in data if pathway in row[pathwaysIndex]]))))
print "%.1f samples with variant per pathway" % utilities.calculateMean(numSamplesPerPathway)

numPathwaysPerSample = []
for sample in uniqueSamples:
    samplePathways = []
    for row in [x for x in data if x[sampleIndex] == sample]:
        samplePathways.append(row[pathwaysIndex])
    numPathwaysPerSample.append(len(set(samplePathways)))
print "%.1f pathways with variant per sample" % utilities.calculateMean(numPathwaysPerSample)

numVariantsPerSamplePerPathway = []
for sample in uniqueSamples:
    sampleData = [x for x in data if x[sampleIndex] == sample]
    for pathway in uniquePathways:
        samplePathwayData = [x for x in sampleData if pathway in x[pathwaysIndex]]
        numVariantsPerSamplePerPathway.append(float(len(samplePathwayData)))
print "%.3f variants per sample per pathway" % utilities.calculateMean(numVariantsPerSamplePerPathway)

numHT = len([1 for x in data if x[descriptionIndex] == "HT"])
numHR = len([1 for x in data if x[descriptionIndex] == "HR"])
print "%i heterozygous variants" % numHT
print "%i homozygous rare variants" % numHR
print "%.2f%% heterozygous variants" % (float(numHT) * 100 / float(numHT + numHR))

inFile.close()

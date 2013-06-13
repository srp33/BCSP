import os, sys, glob
import utilities

inFilePath = sys.argv[1]

inFile = open(inFilePath)

headerItems = inFile.readline().rstrip().split("\t")
sampleIndex = headerItems.index("SampleID")
geneIndex = headerItems.index("GeneID")
pathwaysIndex = headerItems.index("Pathways")

data = [line.rstrip().split("\t") for line in inFile]

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

inFile.close()

import os, sys, glob, re
import utilities

softFilePath = sys.argv[1]
outFilePath = sys.argv[2]

def getSoftSections(startingWith):
    matches = []

    for section in softSections:
        if section.startswith(startingWith):
            matches.append(section)

    return matches

def getData(sectionText, dataStartText):
    data = []

    haveHitDataStart = False
    for line in sectionText.splitlines():
        if line.startswith(dataStartText):
            haveHitDataStart = True

        if haveHitDataStart and not line.startswith("!"):
            data.append(line.rstrip().split("\t"))

    return data

softText = utilities.readTextFromFile(softFilePath)
softSections = softText.split("^")
softSections.pop(0)

platformText = getSoftSections("PLATFORM")[0]
platformData = getData(platformText, "ID")
platformHeaderItems = platformData.pop(0)

probeGeneDict = {}
geneIndex = platformHeaderItems.index("Entrez_Gene_ID")
for row in platformData:
    platformID = row[platformHeaderItems.index("ID")]
    geneID = row[geneIndex]
    probeGeneDict[platformID] = geneID

sampleSections = getSoftSections("SAMPLE")

allSamplesDataDict = {}
for sampleSection in sampleSections:
    sampleID = sampleSection.splitlines()[0].split(" = ")[-1]
    print sampleID

    sampleDataDict = {}

    sampleData = getData(sampleSection, "ID_REF")
    sampleData.pop(0)

    for row in sampleData:
        geneID = probeGeneDict[row[0]]
        value = row[1]

        if geneID != '':
            sampleDataDict[geneID] = sampleDataDict.setdefault(geneID, []) + [value]

    for geneID in sampleDataDict.keys():
        values = [float(x) for x in sampleDataDict[geneID]]
        sampleDataDict[geneID] = utilities.calculateMean(values)

    allSamplesDataDict[sampleID] = sampleDataDict

sampleIDs = sorted(allSamplesDataDict.keys())
geneIDs = sorted(allSamplesDataDict[sampleIDs[0]])

outData = []
outData.append(["Description"] + sampleIDs)

for geneID in geneIDs:
    outData.append([geneID] + [str(allSamplesDataDict[sampleID][geneID]) for sampleID in sampleIDs])

utilities.writeMatrixToFile(outData, outFilePath)

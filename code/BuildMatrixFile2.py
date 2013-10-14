import os, sys, glob
import utilities

inFilePathPattern = sys.argv[1]
outFilePath = sys.argv[2]

uniqueFeatures = None
sampleIDs = []

for inFilePath in glob.glob(inFilePathPattern):
    inData = utilities.readMatrixFromFile(inFilePath)

    print "Getting feature list from %s" % inFilePath
    sampleIDs.append(os.path.basename(inFilePath))

    features = set([row[0] for row in inData])

    if uniqueFeatures == None:
        uniqueFeatures = features
    else:
        uniqueFeatures = uniqueFeatures & features

allDataDict = {}

for inFilePath in glob.glob(inFilePathPattern):
    print "Getting data from %s" % inFilePath
    inData = utilities.readMatrixFromFile(inFilePath)

    inDataDict = {}
    for row in inData:
        inDataDict[row[0]] = row[1]

    allDataDict[os.path.basename(inFilePath)] = inDataDict

outFile = open(outFilePath, 'w')
outFile.write("\t".join([""] + sampleIDs) + "\n")
for feature in uniqueFeatures:
    outFile.write("\t".join([feature] + [allDataDict[sampleID][feature] for sampleID in sampleIDs]) + "\n")
outFile.close()

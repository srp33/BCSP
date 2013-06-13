import os, sys, glob
import utilities

inFilePath = sys.argv[1]
classFilePath = sys.argv[2]
ignorePatientID = sys.argv[3]
includeIDs = sys.argv[4] == "True"
outFilePath = sys.argv[5]

patientClassDict = {}
for line in file(classFilePath):
    if line.startswith("#"):
        continue

    lineItems = line.rstrip().split("\t")
    patientClassDict[lineItems[0]] = lineItems[1]

if os.path.exists(inFilePath):
    data = utilities.transposeMatrix(utilities.readMatrixFromFile(inFilePath))

    features = data.pop(0)
    features.pop(0)
else:
    data = [[patientID] for patientID in patientClassDict.keys()]
    features = []

outFile = open(outFilePath, 'w')
outFile.write("@relation data\n\n")

if includeIDs:
    outFile.write("@attribute id string\n")

for feature in features:
    outFile.write("@attribute %s numeric\n" % feature.replace("'", "prime"))
outFile.write("@attribute Class {%s}\n\n" % ",".join(list(set(patientClassDict.values()))))
outFile.write("@data\n")

for i in range(len(data)):
    row = data[i]
    patientID = row.pop(0).replace(".", "-")

    if patientID == ignorePatientID or patientID not in patientClassDict.keys():
        continue

    if includeIDs:
        row.insert(0, patientID)

    row.append(patientClassDict[patientID])
    outFile.write(",".join(row) + "\n")

outFile.close()

import os, sys, glob
import utilities

matrixFilePath = sys.argv[1]
samplesFilePath = sys.argv[2]
outDirPath = utilities.checkDirPath(sys.argv[3])
outDirFilePattern = sys.argv[4]

data = utilities.readMatrixFromFile(matrixFilePath)

dataPatientIDs = data.pop(0)
samplePatientIDs = [x.replace(".", "-") for x in utilities.readVectorFromFile(samplesFilePath)]

if len(dataPatientIDs) == len(data[0]): #check for description in first column
    dataPatientIDs.pop(0)

variableNames = [x[0] for x in data]

patientValuesDict = {}

for samplePatientID in samplePatientIDs:
    if not samplePatientID in dataPatientIDs:
        continue

    patientValues = [x[dataPatientIDs.index(samplePatientID) + 1] for x in data]

    output = ""
    for i in range(len(variableNames)):
        output += "\t".join([variableNames[i], patientValues[i]]) + "\n"

    outFilePath = outDirPath + samplePatientID + outDirFilePattern
    outFile = open(outFilePath, 'w')
    outFile.write(output)
    outFile.close()

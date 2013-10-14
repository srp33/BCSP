import os, sys, glob
import utilities

inDirPath = sys.argv[1]
inFilePattern = sys.argv[2]
variablesFilePath = sys.argv[3]
dataValueIndex = int(sys.argv[4])
outPatientPrefix = sys.argv[5]
outFilePath = sys.argv[6]

patientIDs = utilities.getPatientIDs(inDirPath, inFilePattern)

variables = None
if variablesFilePath != "None":
    variables = utilities.readVectorFromFile(variablesFilePath)

patientsKeyValuesDict = utilities.getPatientsKeyValuesDict(inDirPath, patientIDs, inFilePattern, dataValueIndex, variables)

outFile = open(outFilePath, 'w')
outFile.write("\t".join(["Key"] + [outPatientPrefix + patientID for patientID in patientIDs]) + "\n")

if variables == None:
    keys = sorted(patientsKeyValuesDict[patientIDs[0]].keys())
else:
    keys = list(set(variables) & set(patientsKeyValuesDict[patientIDs[0]].keys()))

for key in keys:
    outFile.write("\t".join([key] + [patientsKeyValuesDict[patientID][key] for patientID in patientIDs]) + "\n")

outFile.close()

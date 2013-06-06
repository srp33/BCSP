import glob, os, posix, shutil, sys
import utilities
import scipy.stats
from collections import defaultdict

def calculateVarianceMean(values):
    return utilities.calculateVarianceMean(values)

def calculateMean(values):
    return utilities.calculateMean(values)

def calculateTrimmedMean(values):
    return utilities.calculateTrimmedMean(values)

patientID = sys.argv[1]
inFilePath = sys.argv[2]
dataColumnIndex = int(sys.argv[3])
keyProbeFilePath = sys.argv[4]
probeFilePath = sys.argv[5]
minNumProbesPer = int(sys.argv[6])
summarizeFunction = getattr(sys.modules[__name__], sys.argv[7])
outlierSamplesFilePath = sys.argv[8]
outFilePath = sys.argv[9]

outlierSamples = []
if os.path.exists(outlierSamplesFilePath):
    outlierSamples = utilities.readVectorFromFile(outlierSamplesFilePath)

if patientID in outlierSamples:
    print "%s is listed as an outlier, so it won't be summarized" % patientID
    sys.exit(0)

keepProbes = set(utilities.readVectorFromFile(probeFilePath))

print "Getting key/probe dict from %s" % keyProbeFilePath
keyProbeDict = utilities.getKeyProbeDict(keyProbeFilePath)

print "Identifying probes to keep"
for key in keyProbeDict.keys():
    keyProbeDict[key] = list(set(keyProbeDict[key]) & keepProbes)

print "Removing keys with few probes"
keysToRemove = []
for key in keyProbeDict.keys():
    if len(keyProbeDict[key]) < minNumProbesPer:
        keysToRemove.append(key)
for key in keysToRemove:
    del keyProbeDict[key]

print "Building reverse key/value dict"
reverseKeyProbeDict = {}
for key, probes in keyProbeDict.iteritems():
    probes = tuple(sorted(probes))
    reverseKeyProbeDict[probes] = reverseKeyProbeDict.setdefault(probes, []) + [key]

print "Combining duplicate keys"
keyProbeDict2 = {}
for key in reverseKeyProbeDict.keys():
    valueList = reverseKeyProbeDict[key]
    ### Note: If multiple transcripts have the same probes, we just pick the first one in the list.
    modValue = valueList[0]
    keyProbeDict2[modValue] = key

print "Reading data from %s" % inFilePath
probeValuesDict = utilities.getPatientKeyValuesDict(inFilePath, dataColumnIndex, keepProbes)

outFile = open(outFilePath, 'w')

for key in sorted(keyProbeDict2.keys()):
    probeValues = [float(probeValuesDict[probe]) for probe in keyProbeDict2[key]]
    summaryValue = summarizeFunction(probeValues)
    outFile.write("%s\t%.10f\n" % (key, summaryValue))

outFile.close()

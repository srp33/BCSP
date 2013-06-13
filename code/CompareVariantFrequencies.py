import os, sys, glob, shutil, collections, random
import utilities
from operator import itemgetter, attrgetter

inFilePath1 = sys.argv[1]
inFilePath2 = sys.argv[2]
outFilePath = sys.argv[3]

def calculateFisherExact(numNonMuts1, numMuts1, numNonMuts2, numMuts2):
    if ((numNonMuts1 + numNonMuts2) > 0) and ((numMuts1 + numMuts2) > 0):
        return utilities.fisherExactTestLeftTail([[numNonMuts1, numNonMuts2], [numMuts1, numMuts2]])

    return "NA"

inFile1 = open(inFilePath1)
inFile2 = open(inFilePath2)

header1 = inFile1.readline().rstrip().split("\t")
header2 = inFile2.readline().rstrip().split("\t")
outHeader = [[header1[0], header1[1], header1[3], header1[4], header2[3], header2[4], header1[2] + "_Freq", header2[2] + "_Freq", "Fisher_P"]]

dataDict1 = {}
for line in inFile1:
    lineItems = line.rstrip().split("\t")
    dataDict1[lineItems[0]] = lineItems

dataDict2 = {}
for line in inFile2:
    lineItems = line.rstrip().split("\t")
    dataDict2[lineItems[0]] = lineItems

outData = []

for descriptor in (set(dataDict1.keys()) | set(dataDict2.keys())):
    numNonMuts1 = int(dataDict1[descriptor][-3])
    numMuts1 = int(dataDict1[descriptor][-2])
    mutFreq1 = float(dataDict1[descriptor][-1])

    numNonMuts2 = int(dataDict2[descriptor][-3])
    numMuts2 = int(dataDict2[descriptor][-2])
    mutFreq2 = float(dataDict2[descriptor][-1])

    if (numNonMuts1== 0 and numNonMuts2 == 0) or (numMuts1 == 0 and numMuts2 == 0):
        fisherP = "NaN"
    else:
        fisherP = calculateFisherExact(numNonMuts1, numMuts1, numNonMuts2, numMuts2)

    metaInfo1 = dataDict1[descriptor][1]
    metaInfo2 = dataDict2[descriptor][1]

#    meta = ""
#    if len(set([metaInfo1] + [metaInfo2])) == 1:
#        meta = ([metaInfo1] + [metaInfo2])[0]
#    elif len(set([metaInfo1] + [metaInfo2])) > 1:
    meta = ";".join(list([metaInfo1, metaInfo2]))

    outData.append([descriptor, meta, numNonMuts1, numMuts1, numNonMuts2, numMuts2, mutFreq1, mutFreq2, fisherP])

inFile2.close()
inFile1.close()

outData.sort(key=itemgetter(-1))
utilities.writeMatrixToFile(outHeader + outData, outFilePath)

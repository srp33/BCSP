import os, sys, glob
from utilities import *

inFilePattern = sys.argv[1]
outFilePath = sys.argv[2]

outData = []

for inFilePath in glob.glob(inFilePattern):
    pathway = os.path.basename(inFilePath).replace(".gene.ids.txt", "")
    geneIDs = readVectorFromFile(inFilePath)
    outData.append([pathway, ""] + geneIDs)

writeMatrixToFile(outData, outFilePath)

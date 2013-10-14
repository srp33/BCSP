import os, sys, glob
from utilities import *

inFilePattern = sys.argv[1]
outFilePath = sys.argv[2]

outData = []

for inFilePath in glob.glob(inFilePattern):
    pathway = os.path.basename(inFilePath)
    value = readScalarFromFile(inFilePath)
    outData.append([pathway, value])

writeMatrixToFile(outData, outFilePath)

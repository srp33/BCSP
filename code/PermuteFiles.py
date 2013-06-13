import os, sys, glob, random
from utilities import *

inFilePath1 = sys.argv[1]
inFilePath2 = sys.argv[2]
outFilePath1 = sys.argv[3]
outFilePath2 = sys.argv[4]

values1 = readVectorFromFile(inFilePath1)
values2 = readVectorFromFile(inFilePath2)

values = values1 + values2
random.shuffle(values)

permuted1 = [values[i] for i in range(len(values1))]
permuted2 = [values[i] for i in range(len(values1), len(values))]

writeVectorToFile(permuted1, outFilePath1)
writeVectorToFile(permuted2, outFilePath2)

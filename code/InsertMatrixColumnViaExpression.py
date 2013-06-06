import os, sys, glob
import utilities

inFilePath = sys.argv[1]
expression = sys.argv[2]

data = utilities.readMatrixFromFile(inFilePath)
data = map(lambda x: eval("x + [" + expression + "]"), data)

utilities.writeMatrixToFile(data, inFilePath)

import os, sys, glob, math
import utilities

inFilePath = sys.argv[1]
newColumnName = sys.argv[2]
expression = sys.argv[3]
outFilePath = sys.argv[4]

data = utilities.readMatrixFromFile(inFilePath)

if newColumnName != "":
    header = data.pop(0)

data = map(lambda x: eval("x + [str(" + expression + ")]"), data)

if newColumnName != "":
    header.append(newColumnName)
    data.insert(0, header)

utilities.writeMatrixToFile(data, outFilePath)

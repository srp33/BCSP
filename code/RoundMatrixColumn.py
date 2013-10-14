import os, sys, glob
import utilities

inFilePath = sys.argv[1]
numHeaderRows = int(sys.argv[2])
colIndex = int(sys.argv[3])
numPlaces = sys.argv[4]
outFilePath = sys.argv[5]

numberFormatOption = "g"
if len(sys.argv) > 6:
    numberFormatOption = sys.argv[6]

data = utilities.readMatrixFromFile(inFilePath)

def isNumber(x):
    try:
        float(x)
        return True
    except:
        return False

for i in range(numHeaderRows, len(data)):
    if isNumber(data[i][colIndex]):
        modValue = ("%." + numPlaces + numberFormatOption) % float(data[i][colIndex])
    else:
        modValue = data[i][colIndex]
        if len(data[i][colIndex]) > int(numPlaces):
            modValue = data[i][colIndex][:int(numPlaces)] + "..."

    data[i][colIndex] = modValue

utilities.writeMatrixToFile(data, outFilePath)

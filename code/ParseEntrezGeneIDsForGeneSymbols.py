import os, sys, glob
import utilities

symbolsFilePath = sys.argv[1]
entrezGenesSymbolsFilePath = sys.argv[2]
entrezGenesSynonymsFilePath = sys.argv[3]
outFilePath = sys.argv[4]

symbols = utilities.readVectorFromFile(symbolsFilePath)

entrezGenesSymbolsData = utilities.readMatrixFromFile(entrezGenesSymbolsFilePath)
entrezGenesSymbolsDict = {}
for row in entrezGenesSymbolsData:
    entrezGenesSymbolsDict[row[1]] = row[0]

entrezGenesSynonymsData = utilities.readMatrixFromFile(entrezGenesSynonymsFilePath)
entrezGenesSynonymsDict = {}
for row in entrezGenesSynonymsData:
    for synonym in row[1].split("|"):
        entrezGenesSynonymsDict[synonym] = entrezGenesSynonymsDict.setdefault(synonym, []) + [row[0]]

outFile = open(outFilePath, 'w')
for symbol in symbols:
    if entrezGenesSymbolsDict.has_key(symbol):
        entrezID = entrezGenesSymbolsDict[symbol]
    else:
        if entrezGenesSynonymsDict.has_key(symbol):
            entrezIDs = entrezGenesSynonymsDict[symbol]

            if len(entrezIDs) == 1:
                entrezID = entrezIDs[0]
            if len(entrezIDs) > 1:
                entrezID = "[" + symbol + ":" + ",".join(entrezIDs) + "]"
        else:
            entrezID = "[" + symbol + "]"

    outFile.write(entrezID + "\n")
outFile.close()

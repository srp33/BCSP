import os, sys, glob
import utilities

pathwaysFilePath = sys.argv[1]
genePathwayFilePath = sys.argv[2]
geneEntrezFilePath = sys.argv[3]
outFilePathTemplate = sys.argv[4] #KEGG_{PATHWAY_NAME}.gene.ids.txt

pathwaysData = utilities.readMatrixFromFile(pathwaysFilePath)
pathwaysData = [row for row in pathwaysData if not row[0].startswith("#")]
pathwaysDict = {}
for row in pathwaysData:
    pathwaysDict[row[0]] = row[1].upper().replace(" - ", "_").replace(", ", "_").replace(" / ", "_").replace("-", "_").replace(" ", "_").replace("/", "_").replace("(", "").replace(")", "").replace(",", "_").replace("'", "")

genePathwayData = utilities.readMatrixFromFile(genePathwayFilePath)
genePathwayDict = {}
for row in genePathwayData:
    gene = row[0]
    for pathway in row[1].split(" "):
        genePathwayDict[pathway] = genePathwayDict.setdefault(pathway, []) + [gene]

geneEntrezData = utilities.readMatrixFromFile(geneEntrezFilePath)
geneEntrezDict = {}
for row in geneEntrezData:
    geneEntrezDict[row[0].replace("hsa:", "")] = row[1].replace("ncbi-geneid:", "")

for keggPathwayID in pathwaysDict.keys():
    if not genePathwayDict.has_key(keggPathwayID):
        continue

    keggGeneIDs = genePathwayDict[keggPathwayID]
    entrezGeneIDs = [geneEntrezDict[keggGeneID] for keggGeneID in keggGeneIDs]

    outFilePath = outFilePathTemplate.replace("{PATHWAY_NAME}", pathwaysDict[keggPathwayID])
    outFile = open(outFilePath, 'w')
    outFile.write("\n".join(entrezGeneIDs))
    outFile.close()

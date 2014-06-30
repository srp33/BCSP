import os, sys, glob
import utilities

idFilePattern = sys.argv[1]
outFilePath = sys.argv[2]

outFile = open(outFilePath, 'w')

for idFilePath in glob.glob(idFilePattern):
    name = os.path.basename(idFilePath).replace(".gene.ids", "")
    name = name.replace(".txt", "")
    ids = utilities.readVectorFromFile(idFilePath)

    outFile.write("%s\t%s\t%s\n" % (name, name, "\t".join(ids)))

outFile.close()

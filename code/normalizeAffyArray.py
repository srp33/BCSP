import glob,os,sys,time,posix
import utilities
from mycel import MyCEL
from normalize import *

def getMetadata(probeSequenceTabFilePath, probeSequenceTabColIndices):
    probe2seq = {}
    coord = {}

    probeSequenceTabFile = open(probeSequenceTabFilePath)
    probeSequenceTabFile.readline()

    for line in probeSequenceTabFile:
        lineItems = line.rstrip().split("\t")
        probe_id = lineItems[probeSequenceTabColIndices[0]]
        probe_coor_x = lineItems[probeSequenceTabColIndices[1]]
        probe_coor_y = lineItems[probeSequenceTabColIndices[2]]
        probe_seq = lineItems[probeSequenceTabColIndices[3]]

        # The following is for U133 arrays, which don't have standard probe IDs
        #if not probe_id.isdigit():
            #probe_interrogation_position = lineItems[probeSequenceTabColIndices[4]]
            #probe_id = probe_id + "#" + probe_interrogation_position
            #probe_id = probe_id + "#" + probe_coor_x + "_" + probe_coor_y

        probe2seq[probe_id] = probe_seq
        coord[(int(probe_coor_x), int(probe_coor_y))] = probe_id

    probeSequenceTabFile.close()

    return probe2seq, coord

celFilePath = sys.argv[1]
celFileName = os.path.basename(celFilePath)
outFilePath = sys.argv[2]
probeSequenceTabFilePath = sys.argv[3]
probeSequenceTabColIndices = [int(x) for x in sys.argv[4].split("/")]
modelProbesFilePath = sys.argv[5]

cel = MyCEL()
norm = Normalize()

if os.path.exists(outFilePath):
    print "Already processed %s" % outFilePath
else:
    print "Reading annotations"
    probe2seq, coord2probe = getMetadata(probeSequenceTabFilePath, probeSequenceTabColIndices)

    print "Reading " + celFilePath
    probeIntensity = cel.read_cel(celFilePath, coord2probe)

    modelProbes = None
    if os.path.exists(modelProbesFilePath):
        print "Reading model probes file"
        modelProbes = utilities.readVectorFromFile(modelProbesFilePath)
    else:
        if modelProbesFilePath != "None":
            print "No model probes file exists at " + modelProbesFilePath

    print "Normalizing to " + outFilePath
    normValues = norm.normalize(probeIntensity, probe2seq, modelProbes)

    probes = sorted([str(x) for x in probe2seq.keys()])

    exprValues = [normValues[probe][0] for probe in probes]
    probValues = [normValues[probe][1] for probe in probes]

    outFile = file(outFilePath, 'w')
    for i in range(len(probes)):
        outFile.write("%s\t%.9f\t%.9f\n" % (probes[i], exprValues[i], probValues[i]))
    outFile.close()

    print "Done saving to " + outFilePath

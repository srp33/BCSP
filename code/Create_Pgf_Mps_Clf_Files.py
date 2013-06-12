import os, sys, glob
from utilities import *

geneProbeFilePath = sys.argv[1]
probeTabFilePath = sys.argv[2]
outPgfFilePath = sys.argv[3]
outMpsFilePath = sys.argv[4]
outClfFilePath = sys.argv[5]

geneProbeDict = {}
for line in file(geneProbeFilePath):
    lineItems = line.rstrip().split("\t")
    gene = lineItems[0]
    probes = lineItems[1].split(",")
    geneProbeDict[gene] = probes

probeSequenceDict = {}
probeXDict = {}
probeYDict = {}
uniqueX = set()
uniqueY = set()
probeTabFile = open(probeTabFilePath)
probeTabFile.readline()
for line in probeTabFile:
    lineItems = line.rstrip().split("\t")
    probe = lineItems[0]
    x = lineItems[2]
    y = lineItems[3]
    sequence = lineItems[9]
    probeSequenceDict[probe] = sequence
    probeXDict[probe] = x
    probeYDict[probe] = y
    uniqueX.add(int(x))
    uniqueY.add(int(y))

outPgfFile = open(outPgfFilePath, 'w')

outPgfFile.write("#%chip_type=HuEx-1_0-st-v2\n")
outPgfFile.write("#%chip_type=HuEx-1_0-st-v1\n")
outPgfFile.write("#%chip_type=HuEx-1_0-st-ta1\n")
outPgfFile.write("#%lib_set_name=HuEx-1_0-st\n")
outPgfFile.write("#%lib_set_version=r2\n")
outPgfFile.write("#%create_date=Fri May 31 10:18:05 MDT 2013\n")
outPgfFile.write("#%pgf_format_version=1.0\n")
outPgfFile.write("#%header0=probeset_id   type\n")
outPgfFile.write("#%header1=\tatom_id\n")
outPgfFile.write("#%header2=\t\tprobe_id\ttype\tgc_count\tprobe_length\tinterrogation_position\tprobe_sequence\n")

outMpsFile = open(outMpsFilePath, 'w')

outMpsFile.write("#%chip_type=HuEx-1_0-st-v2\n")
outMpsFile.write("#%chip_type=HuEx-1_0-st-v1\n")
outMpsFile.write("#%chip_type=HuEx-1_0-st-ta1\n")
outMpsFile.write("#%lib_set_name=HuEx-1_0-st\n")
outMpsFile.write("#%lib_set_version=r2\n")
outMpsFile.write("#%create_date=Fri May 31 10:18:05 MDT 2013\n")
outMpsFile.write("#%genome-species=Homo sapiens\n")
outMpsFile.write("#%genome-version=hg18\n")
outMpsFile.write("#%genome-version-ucsc=hg18\n")
outMpsFile.write("#%genome-version-ncbi=36\n")
outMpsFile.write("#%genome-version-create_date=2006 March\n")
outMpsFile.write("probeset_id\ttranscript_cluster_id\tprobeset_list\tprobe_count\n")

outClfFile = open(outClfFilePath, 'w')

outClfFile.write("#%chip_type=HuEx-1_0-st-v2\n")
outClfFile.write("#%chip_type=HuEx-1_0-st-v1\n")
outClfFile.write("#%chip_type=HuEx-1_0-st-ta1\n")
outClfFile.write("#%lib_set_name=HuEx-1_0-st\n")
outClfFile.write("#%lib_set_version=r2\n")
outClfFile.write("#%clf_format_version=1.0\n")
outClfFile.write("#%%rows=%i\n" % (max(list(uniqueX)) + 1))
outClfFile.write("#%%cols=%i\n" % (max(list(uniqueY)) + 1))
outClfFile.write("#%header0=probe_id x y\n")

genes = sorted([int(x) for x in geneProbeDict.keys()])
genes = [str(x) for x in genes]

probeCount = 1
for gene in genes:
    print gene
    probes = geneProbeDict[gene]

    outPgfFile.write("%s\n" % gene)

    for probe in probes:
        outPgfFile.write("\t%i\n" % probeCount)
        probeCount += 1

        sequence = probeSequenceDict[probe]
        gc = sequence.count("G") + sequence.count("C")
        outPgfFile.write("\t\t%s\tpm:st\t%i\t%i\t13\t%s\n" % (probe, gc, len(sequence), sequence))

    outMpsFile.write("%s\t%s\t%s\t%s\n" % (gene, " ".join(probes), gene, len(probes)))

sortedProbes = [str(y) for y in sorted([int(x) for x in probeXDict.keys()])]
for probe in sortedProbes:
    outClfFile.write("%s %s %s\n" % (probe, probeXDict[probe], probeYDict[probe]))

outClfFile.close()
outMpsFile.close()
outPgfFile.close()

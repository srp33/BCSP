require(fpc)
source("code/A2R.R")

inFilePath = commandArgs()[7]
outFilePath = commandArgs()[8]

data = t(read.table(inFilePath, sep="\t", header=TRUE, row.names=1, quote="\"", check.names=FALSE))

pdf(outFilePath, height=11, width=8.5)

hclusters <- hclust(dist(data, "euc"), method="ward")
A2Rplot(hclusters, boxes=FALSE)

graphics.off()

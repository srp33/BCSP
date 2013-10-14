require(fpc)

source("code/A2R.R")

inFilePath = commandArgs()[7]
metaFilePath = commandArgs()[8]
metaIDColumnIndex = as.numeric(commandArgs()[9])
metaDescriptionColumnIndex = as.numeric(commandArgs()[10])
outFilePath = commandArgs()[11]

data = t(read.table(inFilePath, sep="\t", header=TRUE, row.names=1, quote="\""))
meta = read.table(metaFilePath, sep="\t", header=TRUE, row.names=NULL, quote="\"")
meta = subset(meta, meta[,metaIDColumnIndex]!="")
rownames(meta) = sub("\\-", "\\.", meta[,metaIDColumnIndex])

commonPatientIDs = intersect(rownames(data), rownames(meta))

data = data[commonPatientIDs,]
meta = meta[commonPatientIDs,]

pdf(outFilePath, height=30, width=130)
#par(mar=c(10, 4.1, 4.1, 12.1))

hclusters <- hclust(dist(data, "euc"), method="ward")
metaValues = meta[,metaDescriptionColumnIndex]
A2Rplot(hclusters, k=length(unique(metaValues)), boxes=FALSE, fact.sup=metaValues)

graphics.off()

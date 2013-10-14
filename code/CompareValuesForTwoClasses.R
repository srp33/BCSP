dataFilePath = commandArgs()[7]
classesFilePath = commandArgs()[8]
classX = commandArgs()[9]
classY = commandArgs()[10]
compareFunction = commandArgs()[11]
outFilePath = commandArgs()[12]

source("code/Common.R")

data = read.table(dataFilePath, sep="\t", stringsAsFactors=F, header=TRUE, row.names=1, check.names=FALSE)
classesData = read.table(classesFilePath, sep="\t", stringsAsFactor=F, header=F, row.names=NULL, check.names=FALSE)
classXSamples = classesData[which(classesData[,2]==classX&classesData[,1]%in%colnames(data)),1]
classYSamples = classesData[which(classesData[,2]==classY&classesData[,1]%in%colnames(data)),1]

xData = data[,classXSamples]
yData = data[,classYSamples]

results = NULL
for (i in 1:nrow(data))
{
  if (compareFunction == "ttest")
  {
    result = t.test(as.numeric(xData[i,]), as.numeric(yData[i,]))$p.value
  } else {
    result = calculateFoldChange(as.numeric(xData[i,]), as.numeric(yData[i,]))
  }

  results = c(results, result)
}

results = cbind(rownames(data), results)
write.table(results, outFilePath, sep="\t", quote=F, row.names=F, col.names=F)

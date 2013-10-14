inFilePath = commandArgs()[7]
familyFilePath = commandArgs()[8]
compareFamily1 = commandArgs()[9]
compareFamily2 = commandArgs()[10]
outFilePath = commandArgs()[11]

data = read.table(inFilePath, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=1)

familyData = read.table(familyFilePath, sep="\t", stringsAsFactors=F, header=TRUE, row.names=NULL)
controlCancerSamples = familyData[which(familyData[,2]%in%c("0", "1")),1]
familialCancerSamples = familyData[which(familyData[,2]%in%c(compareFamily1, compareFamily2)),1]
familialCancerSamples = intersect(familialCancerSamples, rownames(data))

controlCancerValues = data[controlCancerSamples, 3]
familialCancerValues = data[familialCancerSamples, 3]
write.table(t.test(controlCancerValues, familialCancerValues)$p.value, outFilePath, row.names=F, col.names=F, quote=F)

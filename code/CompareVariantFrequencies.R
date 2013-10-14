suppressPackageStartupMessages(library(gdata)) # used for the trim function
suppressPackageStartupMessages(library(Exact))

inFilePath1 = commandArgs()[7]
inFilePath2 = commandArgs()[8]
outFilePath = commandArgs()[9]

data1 = read.table(inFilePath1, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=1)
data2 = read.table(inFilePath2, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=1)
data2 = data2[rownames(data1),]

stats = NULL

for (i in 1:nrow(data1))
{
  data = rbind(as.integer(data1[i,3:4]), as.integer(data2[i,3:4]))

  if (all(data[1,]==0) || all(data[2,]==0) || all(data[,1]==0) || all(data[,2]==0))
  {
    fisherP = NA
    exactP = NA
    fisherStat = "NA"
    exactStat = "NA"
  }
  else {
    fisher.result = fisher.test(data, alternative="greater")
    exact.result = exact.test(data, alternative="greater", to.plot=FALSE)

    fisherStat = fisher.result$estimate
    if (fisherStat %in% c(Inf, -Inf, 0))
      fisherStat = NA

    exactStat = exact.result$test.statistic

    fisherP = fisher.result$p.value
    exactP = min(exact.result$p.value)
  }

  stats = rbind(stats, c(fisherStat, fisherP, exactStat, exactP))
}

colnames(stats) = c("FisherOddsRatio", "Fisher_P", "ExactTestStatistic", "Exact_P")
rownames(stats) = rownames(data1)

outData = merge(data1, data2, by=0)
outData = merge(outData, stats, by.x=1, by.y=0)

colnames(outData)[2] = sub(".x", "", colnames(outData)[2])

for (i in 1:ncol(outData))
  outData[,i] = trim(outData[,i])

outData[,2] = paste(outData[,2], outData[,7], sep=";")
outData = outData[order(as.numeric(outData[,15]), decreasing=FALSE),]
outData = outData[,c(1,2,4,5,9,10,6,11,12,13,14,15)]

write.table(outData, outFilePath, sep="\t", row.names=FALSE, col.names=TRUE, quote=FALSE)

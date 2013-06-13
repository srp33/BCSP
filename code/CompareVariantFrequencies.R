library(gdata) # used for the trim function

inFilePath1 = commandArgs()[7]
inFilePath2 = commandArgs()[8]
outFilePath = commandArgs()[9]

data1 = read.table(inFilePath1, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=NULL)
data2 = read.table(inFilePath2, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=NULL)

freqData = merge(data1, data2, by=1)

cnames = colnames(freqData)

freqData = apply(freqData, 1, function(x) {
  data = rbind(c(as.integer(x[4]), as.integer(x[5])), c(as.integer(x[9]), as.integer(x[10])))
  test.result = fisher.test(data, alternative="greater")
  p = test.result$p.value
  or = test.result$estimate

  if (or %in% c(Inf, -Inf, 0))
    or = NA

  c(x, or, p)
})

freqData = t(freqData)

cnames[2] = sub(".x", "", cnames[2]) # hack
cnames = c(cnames, "Odds_Ratio", "Fisher_P")
colnames(freqData) = cnames

for (i in 1:ncol(freqData))
  freqData[,i] = trim(freqData[,i])

freqData[,2] = paste(freqData[,2], freqData[,7], sep=";")
freqData[,12] = as.numeric(freqData[,12])
freqData[,13] = as.numeric(freqData[,13])
freqData = freqData[order(freqData[,13], decreasing=FALSE),]
freqData = freqData[,c(1,2,4,5,9,10,6,11,12,13)]

write.table(freqData, outFilePath, sep="\t", row.names=FALSE, col.names=TRUE, quote=FALSE)

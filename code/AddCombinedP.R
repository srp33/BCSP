inFilePath = commandArgs()[7]
colIndices = as.integer(strsplit(commandArgs()[8], ",")[[1]])
lowerIsBetter = commandArgs()[9] == "TRUE"
outFilePath = commandArgs()[10]

#######################################################
# From http://en.wikipedia.org/wiki/Fisher's_method
#######################################################
Fisher.test <- function(p)
{
  Xsq <- -2*sum(log(p))
  p.val <- 1-pchisq(Xsq, df = 2*length(p))
  return(p.val)
}
Stouffer.test <- function(p, w) { # p is a vector of p-values
  if (missing(w)) {
    w <- rep(1, length(p))/length(p)
  } else {
    if (length(w) != length(p))
      stop("Length of p and w must equal!")
  }
  Zi <- qnorm(1-p) 
  Z  <- sum(w*Zi)/sqrt(sum(w^2))
  p.val <- 1-pnorm(Z)
  return(p.val)
}
#######################################################
# End from http://en.wikipedia.org/wiki/Fisher's_method
#######################################################

suppressPackageStartupMessages(library(WGCNA))

data = as.matrix(read.table(inFilePath, sep="\t", stringsAsFactors=F, row.names=1, header=T, check.names=F))

#fisherP = apply(data, 1, function(x) {
#  p = as.numeric(x[colIndices])
#  Fisher.test(p)
#})
#stoufferP = apply(data, 1, function(x) {
#  p = as.numeric(x[colIndices])
#  Stouffer.test(p=p)
#})

rankData = NULL
for (i in 1:length(colIndices))
{
  colRanks = rank(data[,colIndices[i]])
  rankData = cbind(rankData, colRanks)
}
meanRanks = apply(rankData, 1, mean)

rankPValueResult = rankPvalue(data[,colIndices])

if (lowerIsBetter)
{
  rankPValues = rankPValueResult$pValueLowRank
} else {
  rankPValues = rankPValueResult$pValueHighRank
}

#correctedFisherP = fisherP * length(fisherP)
#correctedFisherP[correctedFisherP > 1.0] = 1.0

#correctedStoufferP = stoufferP * length(stoufferP)
#correctedStoufferP[correctedStoufferP > 1.0] = 1.0

#data2 = cbind(data, fisherP, correctedFisherP, stoufferP, correctedStoufferP, meanRanks, rankPValues)
#colnames(data2) = c(colnames(data), "Fishers.method.p", "Fishers.method.p.corrected", "Stouffers.method.p", "Stouffers.method.p.corrected", "Mean.Ranks", "Rank.p.values")
data2 = cbind(data, rankPValues)
colnames(data2) = c(colnames(data), "Rank.p.value")

#data2 = data2[order(fisherP),]
#data2 = data2[order(stoufferP),]
data2 = data2[order(rankPValues),]

write.table(data2, outFilePath, sep="\t", col.names=NA, row.names=T, quote=F)

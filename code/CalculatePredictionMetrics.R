inFilePath = commandArgs()[7]
targetClass = commandArgs()[8]
numPermutations = as.integer(commandArgs()[9])
outActualFilePath = commandArgs()[10]
outEmpiricalFilePath = commandArgs()[11]

suppressPackageStartupMessages(library(ROCR))
#suppressPackageStartupMessages(library(rms))

calcAuc = function(actual, prob)
{
  pred = prediction(prob, actual)
  perf = performance(pred, measure="auc", x.measure="cutoff") 
  auc = as.numeric(deparse(as.numeric(perf@y.values)))

  return(auc)
}

#calcCStatistic = function(actual, prob)
#{
#  return(val.prob(prob, actual, pl=FALSE)[2])
#}

data = read.table(inFilePath, sep="\t", stringsAsFactors=FALSE, header=TRUE, row.names=1, check.names=FALSE)

actualClasses = as.integer(data[,1]==targetClass)
actualProbabilities = as.numeric(data[,3])
actualAuc = calcAuc(actualClasses, actualProbabilities)

permutedAucs = NULL

for (i in 1:numPermutations)
{
  set.seed(i)
  permutedProbabilities = sample(actualProbabilities, length(actualProbabilities))
  permutedAucs = c(permutedAucs, calcAuc(actualClasses, permutedProbabilities))
}

empiricalP = sum(permutedAucs >= actualAuc) / numPermutations
empiricalP = empiricalP + 1 / numPermutations
if (empiricalP > 1)
  empiricalP = 1

#cStatistic = calcCStatistic(actualClasses, actualProbabilities)

write(actualAuc, outActualFilePath)
write.table(empiricalP, outEmpiricalFilePath, col.names=FALSE, row.names=FALSE, quote=FALSE)

print("AUC:")
print(actualAuc)
print("Empirical p-value:")
print(empiricalP)
#print("C-statistic:")
#print(cStatistic)

combatSourceFilePath = commandArgs()[7]
inFilePath = commandArgs()[8]
batchFilePath = commandArgs()[9]
outFilePath = commandArgs()[10]

source(combatSourceFilePath)

tempCombinedExprFilePath = sub("\\.txt", "_tmp.txt", inFilePath)
tempCombinedExprOutFilePath = paste('Adjusted', tempCombinedExprFilePath, '.xls', sep='_') #This format comes from ComBat

print(paste("Reading data from", inFilePath))
data = read.table(inFilePath, sep="\t", header=TRUE, row.names=1, quote="\"")

print(paste("Writing data to", tempCombinedExprFilePath))
write.table(data, file=tempCombinedExprFilePath, quote=FALSE, row.names=FALSE, sep="\t")

print("Running combat")
ComBat(tempCombinedExprFilePath, batchFilePath, prior.plots=FALSE)

combined_adjust = read.table(tempCombinedExprOutFilePath, header=T)
rownames(combined_adjust) = rownames(data)
colnames(combined_adjust) = sub("\\.", "-", colnames(combined_adjust))

write.table(t(c("Description", colnames(combined_adjust))), file=outFilePath, quote=FALSE, col.names=FALSE, row.names=FALSE, sep="\t")
write.table(combined_adjust, file=outFilePath, quote=FALSE, col.names=FALSE, row.names=TRUE, sep="\t", append=TRUE)

unlink(tempCombinedExprFilePath)
unlink(tempCombinedExprOutFilePath)

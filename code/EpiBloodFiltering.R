bloodFilePath = commandArgs()[7]
epiFilePath = commandArgs()[8]
dataFilePath = commandArgs()[9]
outKeepFilePath = commandArgs()[10]
outFilterFilePath = commandArgs()[11]
pValueCutoff = 0.01

blood = as.matrix(read.table(bloodFilePath, header=TRUE, sep='\t', row=1)) 

ep = as.matrix(read.table(epiFilePath, sep='\t', header=T, row=1))
#for (i in 1:ncol(ep)){m=mean(ep[,i],na.rm=T);ep[is.na(ep[,i]),i]=m} # replace missing ep values with mean
ep = ep[,-c(9,10,19,24)] ### remove ContrAge1st, Pregnant, PeriodAge, Polyps
ep = ep[-which(rownames(ep) %in% c("Utah__C10","Utah__G12")),]  ## incomplete data

data = read.table(dataFilePath, sep="\t", row.names=1, stringsAsFactors=FALSE, header=TRUE, quote="\"")

#### Pre-filter the genes according to association with blood predictors: CD19, CD4, CD4CD8, CD8, CD3, nk-cells
samples = intersect(rownames(blood), colnames(data))
blood = blood[samples,]
bdat = data[,samples] # reduce down to the set of samples with blood predictors

means = apply(bdat, 1, mean)

SSR=apply((bdat-means%*%t(rep(1,ncol(bdat))))^2,1,sum) # reduced SS

X=cbind(1,blood)
A=solve(t(X)%*%X)
beta=A%*%t(X)%*%as.matrix(t(bdat))
SSF=apply((t(bdat)-X%*%beta)^2,2,sum) #Full SS
Fstats=((SSR-SSF)/(ncol(X)-1))/(SSF/(ncol(bdat)-ncol(X)))
pvals=1-pf(Fstats,ncol(X)-1,ncol(bdat)-ncol(X));
#hist(pvals)

newdat1 = data[which(pvals>pValueCutoff),]

#### Pre-filter the genes according to association with clinical predictors: Age, Edu, Marital, RelPref, Health, Physical, MenstrAge, Contr, ContrAge1st, Pregnant, PregnantNo, TtlLiveBirth, FirstBirthAge, LastBirthAge, BrFeed, UnableChild, Period, PeriodStop, PeriodAge, Tamoxifen, Alcohol, CigSmoke, Employment, Polyps, ImmunoDisorder, Hypertension, AntiInfDrug

samples = intersect(rownames(ep), colnames(newdat1))
ep = ep[samples,]

epdat=newdat1[,samples] #
means = apply(epdat, 1, mean)
SSR=apply((epdat-means%*%t(rep(1,ncol(epdat))))^2,1,sum) # reduced SS

pvals=NULL
for (i in (1:ncol(ep))){
  if (i %in%c(1,2,7,9,10,11,12)){
    Xep=cbind(1,ep[,i])
  }else{
    Xep=rep(1,nrow(ep))
    tmp=as.factor(ep[,i])
    for (j in levels(tmp)[-1]){
    Xep=cbind(Xep, tmp==j)
    }
  }
  A=solve(t(Xep)%*%Xep)
  beta=A%*%t(Xep)%*%as.matrix(t(epdat))
  SSF=apply((t(epdat)-Xep%*%beta)^2,2,sum) #Full SS
  Fstats=((SSR-SSF)/(ncol(Xep)-1))/(SSF/(ncol(epdat)-ncol(Xep)))
  pvals=cbind(pvals,1-pf(Fstats,ncol(Xep)-1,ncol(epdat)-ncol(Xep)))
  #hist(pvals)
}

pvalf=function(p){any(p>pValueCutoff)}
keep=apply(pvals,1,pvalf)

newdat2=newdat1[keep,]

keepTranscripts = rownames(newdat2)
filterTranscripts = setdiff(rownames(data), keepTranscripts)

print(paste("Keeping", length(keepTranscripts), "transcripts"))
print(paste("Filtering", length(filterTranscripts), "transcripts"))
write(keepTranscripts, outKeepFilePath)
write(filterTranscripts, outFilterFilePath)

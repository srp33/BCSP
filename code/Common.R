convertFactorsToCharacter = function(x)
{
  if (is.factor(x))
    return(as.character(levels(x)[x]))
  else
    return(x)
}

convertFactorsToNumeric = function(x)
{
  if (is.factor(x))
    return(as.numeric(levels(x)[x]))
  else
    return(x)
}

matrixplot = function(data, main)
{
  plot(NULL, xlim=c(1, ncol(data)), ylim=c(min(data), max(data)), main=main, xlab="", ylab="", xaxt="n")
  axis(1, at=1:ncol(data), labels=colnames(data), las=2)

  for (i in 1:ncol(data))
    points(rep(i, nrow(data)), data[,i])
}

scaleToZeroOne = function(x)
{
  (x - min(x)) / max((x-min(x)))
}

getAuc = function(data, classColIndex, targetClass, valueColIndex, plot=TRUE)
{
  suppressPackageStartupMessages(library(ROCR))

#  getMultiClassAuc = function()
#  {
#    AUCs = NULL
#    weights = NULL
#
#    for (i in 4:ncol(predictionMatrix))
#    {
#      className = colnames(predictionMatrix)[i]
#
#      classProbabilities = as.numeric(predictionMatrix[,i])
#      classActual = as.integer(predictionMatrix[,2]==className)
#
#      if (length(unique(classActual))==1)
#      {
#        print("All instances predicted to have same class, so defaulting to 0.5")
#        AUC = 0.5
#      }
#      if (length(unique(classActual))==2)
#      {
#        pred = prediction(classProbabilities, classActual)
#        perf = performance(pred, measure="auc", x.measure = "cutoff")
#        AUC = deparse(as.numeric(perf@y.values))
#      }
#
#      AUCs = c(AUCs, as.numeric(AUC))
#      weights = c(weights, sum(classActual))
#    }
#
#    weightedMeanAUC = sum(AUCs*(weights/nrow(predictionMatrix)))
#    return(weightedMeanAUC)
#  }

  getTwoClassAuc = function()
  {
    classes = unique(data[,classColIndex])

    if (length(classes) < 2)
    {
      print("No two classes, so AUC default is 0.5.")
      return(0.5)
    }

    values = as.numeric(data[,valueColIndex])
    actual = as.integer(data[,classColIndex]==targetClass)

    pred = prediction(values, actual)
    perf = performance(pred, measure="auc", x.measure="cutoff") 
    auc = as.numeric(deparse(as.numeric(perf@y.values)))

    if (plot)
    {
      par(mar=c(5.1, 4.1, 0.5, 0.5))
      plot(performance(pred, measure="tpr", x.measure="fpr"), main="")
      text(.8, .1, paste("AUC =", round(auc, 3)))
      abline(0, 1, lty=2)
      par(mar=c(5.1, 4.1, 2.1, 2.1))
    }

    return(auc)
  }

  #if (length(valueColIndices)==1)
    return(getTwoClassAuc())
  #else
  #  return(getMultiClassAuc())
}

plotROC = function(actual, probabilities, targetClass)
{
  if (length(unique(actual)) < 2)
  {
    print("No two classes, so no ROC curve.")
  }
  else {
    par(mar=c(4.4, 4.7, 0.6, 0.6))

    cex = 2.2
    preds = prediction(probabilities, as.integer(actual==targetClass))
    plot(performance(preds, measure = "tpr", x.measure = "fpr"), lwd=5, main="", box.lwd=2, xaxis.cex.axis=cex, xaxis.lwd=2, yaxis.cex.axis=cex, yaxis.lwd=2, cex.lab=cex)
    auc = as.numeric(deparse(as.numeric(performance(preds, measure="auc", x.measure="cutoff")@y.values)))
    text(.75, .25, paste("AUC =", round(auc, 3)), cex=cex)
    abline(0, 1, lty=2, lwd=3)
    box(lwd=2)
  
    par(mar=c(5.1, 4.1, 2.1, 2.1))
  }
}

plotROC2 = function(actual, probabilities, targetClass, plotCI=FALSE)
{
  if (length(unique(actual)) < 2)
  {
    print("No two classes, so no ROC curve.")
  }
  else {
    par(mar=c(4.5, 4.7, 0.0, 0.5))

    library(pROC)
    roc_result = roc(actual ~ probabilities, ci=TRUE, plot=TRUE, print.auc=FALSE)
    lowerBoundAuc = format(roc_result$ci[1], digits=3)
    midAuc = format(roc_result$ci[2], digits=3)
    upperBoundAuc = format(roc_result$ci[3], digits=3)

    if (plotCI)
    {
      ci(roc_result)
      sens.ci <- ci.se(roc_result)
      plot(sens.ci, type="shape", col="lightblue")
      plot(sens.ci, type="bars")
#      plot(roc_result, add=TRUE)
    }

    text(0.15, 0.00, labels=paste("AUC: ", midAuc, " (", lowerBoundAuc, "-", upperBoundAuc, ")", sep=""))

    par(mar=c(5.1, 4.1, 2.1, 2.1))
  }
}

getAccuracy = function(predictions, actual)
{
  if (length(predictions) != length(actual))
    stop("The number of predictions must be the same as the number of actual values in calculating accuracy.")

  return(sum(predictions==actual) / length(predictions))
}

plotMatrix = function(x, ...)
{
  # From http://www.phaget4.org/R/image_matrix.html

     min <- min(x)
     max <- max(x)
     yLabels <- rownames(x)
     xLabels <- colnames(x)

     # Piccolo add
     xLabels = gsub("_", "\n", xLabels)
     yLabels = gsub("_", "\n", yLabels)

     title <-c()
  # check for additional function arguments
  if( length(list(...)) ){
    Lst <- list(...)
    if( !is.null(Lst$zlim) ){
       min <- Lst$zlim[1]
       max <- Lst$zlim[2]
    }
    if( !is.null(Lst$yLabels) ){
       yLabels <- c(Lst$yLabels)
    }
    if( !is.null(Lst$xLabels) ){
       xLabels <- c(Lst$xLabels)
    }
    if( !is.null(Lst$title) ){
       title <- Lst$title
    }
  }
# check for null values
if( is.null(xLabels) ){
   xLabels <- c(1:ncol(x))
}
if( is.null(yLabels) ){
   yLabels <- c(1:nrow(x))
}

layout(matrix(data=c(1,2), nrow=1, ncol=2), widths=c(4,1), heights=c(1,1))

 # Red and green range from 0 to 1 while Blue ranges from 1 to 0
 ColorRamp <- rgb( seq(0,1,length=256),  # Red
                   seq(0,1,length=256),  # Green
                   seq(1,0,length=256))  # Blue
 ColorLevels <- seq(min, max, length=length(ColorRamp))

 # Reverse Y axis
 reverse <- nrow(x) : 1
 yLabels <- yLabels[reverse]
 x <- x[reverse,]

 # Data Map
 par(mar = c(3,8.5,2.5,2))
 image(1:length(xLabels), 1:length(yLabels), t(x), col=ColorRamp, xlab="",
 ylab="", axes=FALSE, zlim=c(min,max))
 if( !is.null(title) ){
    title(main=title)
 }
axis(BELOW<-1, at=1:length(xLabels), labels=xLabels, cex.axis=0.7)
 axis(LEFT <-2, at=1:length(yLabels), labels=yLabels, las= HORIZONTAL<-1,
 cex.axis=0.7)

 # Color Scale
 par(mar = c(3,2.5,2.5,2))
 #par(mar = c(3,2.5,2.5,2))
 image(1, ColorLevels,
      matrix(data=ColorLevels, ncol=length(ColorLevels),nrow=1),
      col=ColorRamp,
      xlab="",ylab="",
      xaxt="n")

 layout(1)
}

plotCorrMatrix = function(data, title="")
{
  corr <- cor(x = data, method = "spearman")
  corr <- corr[,ncol(corr):1]
  plotMatrix(corr, title=title)


  # From http://sphaerula.com/legacy/R/correlationPlot.html#plotCorrelation
#  library(package = ISwR)
#
#  red <- c(seq( from = 1.0,  to = 0.0, by = -0.1 ), seq( from = 0.05, to = 0.5, length = 10 )) 
#  green <- red
#  blue <- c(rep( x = 0.0, times = 10 ), seq( from = 0.0, to = 1.0, by = 0.1 ))
#  #colors <- rgb(red = red, green = green, blue = blue)
#  colors = terrain.colors(50)
#
#  corr <- cor(x = t(data), method = "spearman")
#
#  ##  Reverse the columns of the matrix so it will be drawn correctly.
#  n = ncol(corr)
#  corr2 <- corr[, n:1 ]
#
#  par(mar=c(7.1, 7.1, 7.1, 2.1))
#  image(z=corr2, axes=FALSE, col=colors, zlim=c(-1.0, 1.0))
#  axis(side=2, labels=colnames(corr2), at=seq(0, 1, length = length(rownames(corr2))), cex.axis = 0.8, las = 2)
#  axis(side=3, labels=rownames(corr2), at=seq(0, 1, length = length(colnames(corr2))), cex.axis = 0.8, las = 2)
}

getAnovaPValue = function(values, familyIndices)
{
  aov.result = aov(values ~ as.factor(familyIndices))
  aov.p = summary(aov.result)[[1]][[5]][1]
  #print(summary(aov.result))
  #print(model.tables(aov.result, "means"), digits=4)

  return(aov.p)
}

plotValuesPerFamilyBoxPlot = function(values, familyDescription, familyNumber, cohort, cohortOrder, ylab, ylim, y.at, xlab.split=",", includeAnova=FALSE, col=1)
{
  # These are customizations requested by Andrea temporarily
  #familyNumber[familyNumber==3] = 6
  #familyNumber[familyNumber==4] = 3
  #familyNumber[familyNumber==6] = 4

  data = cbind(values, familyDescription, familyNumber, cohort)
  data = data[order(familyNumber),]
  values = as.numeric(data[,1])
  familyDescription = data[,2]
  familyNumber = as.integer(data[,3])
  cohort = data[,4]

  familyIndices = sapply(familyNumber, function(x) {which(unique(familyNumber)==x)})

  # These are customizations requested by Andrea temporarily
  #familyDescription = gsub("No Family History, No Cancer", "Control", familyDescription)
  #familyDescription = gsub("Sporadic Cancer", "Sporadic,Cancer", familyDescription)
  #ylab = "Probability of Cancer Risk"
#  familyDescription = gsub("No Family History, No Cancer", "No fam. hist. no cancer", familyDescription)
#  familyDescription = gsub("Sporadic Cancer", "Sporadic cancer", familyDescription)
#  familyDescription = gsub("Cancer", "cancer", familyDescription)
#  familyDescription = gsub("No cancer", "no cancer", familyDescription)
  #ylab = "HBC Score"

  familyDescriptionLabels = unique(familyDescription)
  familyDescriptionLabels = gsub(", ", "\n", familyDescriptionLabels)

  cex = 2.2
  par(mar=c(4.4, 4.7, 0.6, 0.6))
  boxplot(values ~ familyIndices, ylab=ylab, ylim=ylim, xaxt="n", yaxt="n", cex.lab=cex, lwd=3, col=col, range=0)
  axis(1, 1:(length(unique(familyIndices))), familyDescriptionLabels, lwd.ticks=0, mgp=c(3.0, 3.2, 0), cex.axis=cex)
  axis(2, y.at, lwd.ticks=2, cex.axis=cex)
  box(lwd=3)

  if (includeAnova)
  {
    anova.p = getAnovaPValue(values, familyIndices)
    text(x=min(familyIndices), y=min(values), labels=paste("p = ", format(anova.p, digits=2), sep=""), offset=0, pos=3, cex=cex)
  }
}

plotValuesPerFamily = function(values, familyDescription, familyNumber, cohort, cohortOrder, ylab, ylim, y.at, xlab.split=",")
{
  data = cbind(values, familyDescription, familyNumber, cohort)
  data = data[order(familyNumber),]
  values = as.numeric(data[,1])
  familyDescription = data[,2]
  familyNumber = as.integer(data[,3])
  cohort = data[,4]

  colorScheme = brewer.pal(8, "Set1")

  familyIndices = sapply(familyNumber, function(x) {which(unique(familyNumber)==x)})
  numFamilies = length(unique(familyIndices))
  colors = colorScheme[familyIndices]

  cohortIndices = sapply(cohort, function(x) {which(cohortOrder==x)})
  pch = c(21,23,24,25)[cohortIndices]

  x = jitter(familyIndices - 1, factor=2.0)
  xlim = c(-0.35, numFamilies - 1 + 0.35)

  par(xpd=TRUE, mar=c(4.0, 4.4, 0.5, 0.5))
  plot(x, values, bg=colors, pch=pch, cex=1.5, xlab="", xaxt="n", yaxt="n", ylab=ylab, main="", xlim=xlim, ylim=ylim, cex.lab=1.5)
  axis(1, 0:(numFamilies-1), formatAxisNames(unique(familyDescription), xlab.split), lwd.ticks=2)

  axis(2, y.at, cex.axis=1.5, lwd.ticks=2)
  box(lwd=2)

  if (length(unique(pch)) > 1)
    legend("topright", legend=cohortOrder[cohortOrder%in%cohort], pch=unique(sort(pch)), inset=0.02, pt.cex=1.5)
}

plotClassValues = function(values, familyDescription, familyNumber, cohort, actual, targetClass, cohortOrder, ylab, ylim, y.at)
{
  data = cbind(values, familyDescription, familyNumber, cohort, actual)
  data = data[order(familyNumber),]
  values = as.numeric(data[,1])
  familyDescription = data[,2]
  familyNumber = as.numeric(data[,3])
  cohort = data[,4]
  actual = data[,5]

  groupDescriptors = unique(familyDescription)
  colorIndices = sapply(familyDescription, function(x) {which(groupDescriptors==x)})
  colorScheme = brewer.pal(8, "Set1")
  colors = colorScheme[colorIndices]
  legendColors = unique(colors)

  cohortIndices = sapply(cohort, function(x) {which(cohortOrder==x)})
  pch = c(21,23,24,25)[cohortIndices]

  x = jitter(as.integer(actual==targetClass), factor=1.5)
  xlim = c(-0.35, 1.35)

  par(xpd=TRUE, mar=c(4.0, 4.4, 0.5, 15.2))
  plot(x, values, bg=colors, pch=pch, cex=1.5, xlab="", xaxt="n", yaxt="n", ylab=ylab, main="", xlim=xlim, ylim=ylim, cex.lab=1.5)
  axis(1, 0:1, paste(unique(actual), "\nPatients"), cex.axis=1.5, mgp=c(3, 2.7, 0), lwd.ticks=2)
  axis(2, at=y.at, cex.axis=1.5, lwd.ticks=2) #padj=0.4

  legend.pch = rep(22, length(legendColors))
  if (length(unique(pch)) == 1)
    legend.pch = unique(pch)

  legend.coord = legend(1.45, max(values), legend=groupDescriptors, pt.bg=legendColors, pt.cex=1.5, pch=legend.pch, box.lwd=2)

  if (length(unique(pch)) > 1)
    legend(legend.coord$rect$left, legend.coord$rect$top - legend.coord$rect$h - 0.03, legend=cohortOrder[cohortOrder%in%cohort], pch=unique(sort(pch)), box.lwd=2, pt.cex=1.5)

  box(lwd=2)
}

plotClassValuesBoxPlot = function(values, cohort, theClass, cohortOrder, classOrder, ylab)
{
  data = cbind(values, cohort, theClass)

  par(mfrow=c(1,3))
  for (i in 1:length(cohortOrder))
  {
    displayCohort = cohortOrder[i]
    cohortData = data[which(data[,2]==displayCohort),]
    cohortClass1Values = as.numeric(cohortData[which(cohortData[,3]==classOrder[1]),1])
    cohortClass2Values = as.numeric(cohortData[which(cohortData[,3]==classOrder[2]),1])

    thisYlab = ylab
    if (i > 1)
      thisYlab = ""

    t.test.p = t.test(cohortClass1Values, cohortClass2Values)$p.value
    t.test.p = format(t.test.p, digits=2, nsmall=3)
    #foldChange = calculateFoldChange(cohortClass2Values, cohortClass1Values)

    par(mar=c(5.1, 4.4, 2.1, 0.5))
    boxplot(as.numeric(cohortData[,1])~factor(cohortData[,3], levels=classOrder), ylab=thisYlab, main=displayCohort, cex=1.5, cex.lab=1.5, cex.main=1.5, cex.axis=1.5, lwd=2, xlab=paste("(p-value: ", t.test.p, ")", sep=""), ylim=c(min(values), max(values)))
    box(lwd=2)
  }
}

formatAxisNames = function(x, splitChar)
{
  maxSize = 0

  for (y in x)
  {
    z = strsplit(y, splitChar)[[1]]
    if (length(z) > maxSize)
      maxSize = length(z)
  }

  formatted = NULL

  for (y in x)
  {
    z = strsplit(y, splitChar)[[1]]
    while (length(z) < maxSize)
      z = c(z, "")

    formatted = c(formatted, paste(z, collapse="\n"))
  }

  formatted
}

calculateFoldChange = function(x, y)
{
  overallMin = min(c(x, y))

  values1 = x - overallMin + 1
  values2 = y - overallMin + 1

  mean1 = median(values1)
  mean2 = median(values2)

  mean1 / mean2
}



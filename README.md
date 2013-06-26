Prerequisites
====

  python (we used v2.6.6)

  scipy/numpy

  R

    gdata package

    e1071 package

  FastQC

  samtools-0.1.18

  bamtools

  bwa-0.6.1

  GenomeAnalysisTK-1.5-3-gbb2c10b

  GenomeAnalysisTK-2.3-4-g57ea19f

  picard-tools-1.64

  snpEff_3_1

  dbNSFP2

Notes
====

Raw microarray files can be downloaded from GEO (GSE47682). Place them in the "raw" directory. They should be separated into subdirectories called utah, ontario1, and ontario2.

For the gene expression analyses, you may get slightly different results depending on version of scipy/numpy you use. (We used scipy version 0.8.0 and numpy version 1.5.1.) However, the downstream results should be similar.

We identified genes whose expresson was correlated with demographic / clinical variables and excluded those (described in manuscript). The data file containing these variables is not here. Please contact the authors for a copy of that file.

Please contact stephen.piccolo [at] hsc [dot] utah [dot] edu if you run into any problems.

Prerequisites
====

  python (we used v2.6.6)

  scipy/numpy

  R

    gdata package

    e1071 package

Analysis
====

This repository contains code and scripts that were used for the manuscript entitled, "Gene-expression patterns in peripheral blood classify familial breast cancer susceptibility."

To run the analysis, you would execute the ```go``` bash script. This calls various other scripts.

Notes
====

Raw microarray files can be downloaded from Gene Expression Omnibus (GSE47682, GSE12517). Place them in the "raw" directory. They should be separated into subdirectories called utah, ontario1, and ontario2, and GSE12517.

For the gene expression analyses, you may get slightly different results depending on version of scipy/numpy you use. (We used scipy version 0.8.0 and numpy version 1.5.1.) However, the downstream results should be similar.

We identified genes whose expresson was correlated with demographic / clinical variables and excluded those (described in manuscript). The data file containing these variables is not here. Please contact the authors for a copy of that file.

Please contact stephen_piccolo [at] byu [dot] edu with any questions.

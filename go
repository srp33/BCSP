#!/bin/bash

# Gene expression analyses
./scripts/preprocess
./scripts/mlflex
./scripts/postprocess

# Exome sequencing analyses
./scripts/tcga
./scripts/utahexome

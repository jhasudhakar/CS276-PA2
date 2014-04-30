#!/bin/bash

if [ $# -lt 4 ]; then
    echo "Usage: ./run-dev.sh <data_dir> <uniform|empirical> <queres_file> <gold_file>"
    echo " e.g.: ./run-dev.sh data/ uniform data/dev.queries.txt data/dev.gold.txt"
    exit 1
fi

java -Xmx2048m -cp "bin:jars/*" edu.stanford.cs276.RunDevSet $@

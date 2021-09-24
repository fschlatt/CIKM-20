#!/bin/bash
## variables
jar="./java/bootstrapping/target/bootstrapping-1.0-SNAPSHOT-jar-with-dependencies.jar"
data0="/media/data/causenet-data/bootstrapping/seeds.csv"
data1="/media/data/causenet-data/lucene-index/"
data2="/media/data/causenet-data/bootstrapping/"

## processing
java -jar -Xmx150G -Xms150G $jar $data0 $data1 $data2

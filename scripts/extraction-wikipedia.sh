#!/bin/bash
## variables
jar="./java/extraction/target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"
data0="/mnt/ceph/storage/corpora/corpora-thirdparty/corpus-wikipedia/wikimedia-snapshots/enwiki-20210601/enwiki-20210601-pages-articles.xml.bz2" 
data1="/mnt/ceph/storage/causenet-data/bootstrapping/2-patterns" 
data2="/user/fschlatt/wikipedia"

## processing
spark-submit \
    --name "CauseNet Wikipedia Extraction" \
    --master yarn \
    --deploy-mode cluster \
    --num-executors 40 \
    --executor-memory 12g \
    --conf spark.yarn.submit.waitAppCompletion=false \
    --conf spark.executor.memoryOverhead=12000 \
    --conf spark.dynamicAllocation.enabled=false \
    --conf spark.driver.memoryOverhead=2000 \
    --conf spark.network.timeout=600 \
    $jar $data0 $data1 $data2

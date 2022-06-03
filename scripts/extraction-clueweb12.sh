#!/bin/bash
## variables
jar="./java/extraction/target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"

data1="/mnt/ceph/storage/data-in-progress/data-research/web-search/health-question-answering/causenet-data/bootstrapping/2-patterns"
data2="/mnt/ceph/storage/data-in-progress/data-research/web-search/health-question-answering/causenet-data/external/stop-word-lists/enStopWordList.txt"
data4="/mnt/ceph/storage/data-in-progress/data-research/web-search/health-question-answering/causenet-data/external/ignore-html-lists/empty.txt"

while read part
do
    echo $part
    data0="\"/corpora/corpora-thirdparty/corpus-clueweb/12/*/ClueWeb12_"$part"/*/*.warc.gz\""
    data3="/user/fschlatt/clueweb12/part"$part
    hdfs dfs -rm -r -f $data3

    spark-submit \
        --name "CauseNet ClueWeb12 Extraction Part "$part \
        --master yarn \
        --deploy-mode cluster \
        --num-executors 60 \
        --executor-memory 16g \
        --driver-memory 16g \
        --conf spark.executor.memoryOverhead=2g \
        --conf spark.dynamicAllocation.enabled=false \
        --conf spark.driver.memoryOverhead=2g \
        --conf spark.network.timeout=600 \
        --conf spark.yarn.maxAppAttempts=1 \
        $jar $data0 $data1 $data2 $data3 $data4

done < clueweb12-parts.txt
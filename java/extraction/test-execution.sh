#!/bin/bash
mvn clean install

## variables
jar="target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"

data0="\"s3a://corpus-commoncrawl-main-2021-10/crawl-data/CC-MAIN-2021-10/segments/*/warc/*.warc.gz\""
data1="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/bootstrapping/2-patterns"
data2="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/external/stop-word-lists/enStopWordList.txt"
data3="/user/fschlatt/common-crawl"
data4="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/external/ignore-html-lists/empty.txt"

export HADOOP_USER_NAME=fschlatt
export HADOOP_CONF_DIR=/mnt/ceph/storage/data-tmp/2021/fschlatt/hadoop-conf

spark-submit \
    --name "CauseNet CommonCrawl Extraction" \
    --deploy-mode cluster \
    --num-executors 60 \
    --executor-memory 8g \
    --conf spark.yarn.submit.waitAppCompletion=false \
    --conf spark.executor.memoryOverhead=12000 \
    --conf spark.dynamicAllocation.enabled=false \
    --conf spark.driver.memoryOverhead=2000 \
    --conf spark.network.timeout=600 \
    --master local \
    $jar $data0 $data1 $data2 $data3 $data4

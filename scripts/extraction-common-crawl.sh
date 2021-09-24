#!/bin/bash
## variables
jar="./java/extraction/target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"

data1="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/bootstrapping/2-patterns"
data2="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/external/stop-word-lists/enStopWordList.txt"
data4="/mnt/ceph/storage/data-in-progress/data-research/web-search/medical-question-answering/causenet-data/external/ignore-html-lists/empty.txt"

while read segment
do
    echo $segment
    data0="\"s3a://corpus-commoncrawl-main-2021-10/crawl-data/CC-MAIN-2021-10/segments/"$segment"/warc/*.warc.gz\""
    data3="/user/fschlatt/common-crawl/"$segment
    hdfs dfs -rmdir --ignore-fail-on-non-empty $data3

    spark-submit \
        --name "CauseNet CommonCrawl Extraction "$segment \
        --master yarn \
        --deploy-mode cluster \
        --num-executors 120 \
        --executor-memory 8g \
        --driver-memory 8g \
        --conf spark.executor.memoryOverhead=2g \
        --conf spark.dynamicAllocation.enabled=false \
        --conf spark.driver.memoryOverhead=2g \
        --conf spark.network.timeout=600 \
        --conf spark.yarn.maxAppAttempts=1 \
        $jar $data0 $data1 $data2 $data3 $data4

done < common-crawl-segments.txt
#!/bin/bash
export HADOOP_HOME='/opt/hadoop-2.6.0'
/opt/hive-1.1.0/bin/hive -e "DROP TABLE $3; set hive.auto.convert.join=true; CREATE TABLE $3 ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' AS SELECT NATIONKEY, TOTALPRICE FROM $1 LEFT JOIN $2 ON $1.CUSTKEY=$2.CUSTKEY ;" 


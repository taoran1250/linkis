#!/bin/bash

job_id=$1
log_path=$2
yyy_mm=$3
yyyy_mm_dd=$4
created_time_date=$5

# 远端引擎日志分组写入
function remote_engine_logs() {
  cd $log_path
  # 获取首行日志信息
  #  first_row=$(head -n 1 ./stdout | cut -b 1-23)
  #  first_row_timestamp=$(date -d "$first_row" +%s)
  #  # 首行日期大于任务创建时间，代表已经滚动
  #  if [ "$first_row_timestamp" -gt "$created_time_date" ]; then
  #    if [ -d "./$yyy_mm" ]; then
  #      # 引擎日志已滚动
  #      echo -e "$(cat ./stdout ./$yyy_mm/linkis-log-$yyyy_mm_dd-* | grep "JobId-$job_id")" >"./"$job_id"_engineconn.log"
  #    fi
  #  else
  #    # 引擎日志未滚动
  #    echo -e "$(cat ./stdout | grep "JobId-$job_id")" >"./"$job_id"_engineconn.log"
  #  fi
  echo -e "$(cat ./stdout | grep "JobId-$job_id")" >"./"$job_id"_engineconn.log"
}

function remote_link_logs() {
  cd $log_path
  # 获取首行日志信息
  first_row=$(head -n 1 ./linkis-cg-entrance.log | cut -b 1-23)
  first_row_timestamp=$(date -d "$first_row" +%s)
  # 首行日期大于任务创建时间，代表已经滚动
  if [ "$first_row_timestamp" -gt "$created_time_date" ]; then
    # 链路日志已滚动 （grep "JobId-$1" 必须双引号，命令行可以单引号）
    # echo -e > 写入本地，必须带引号，否则不能换行
    echo -e "$(cat ./linkis-cg-entrance.log ./$yyy_mm/linkis-cg-entrance/linkis-log-$yyy_mm* | grep "JobId-$job_id")" >"./"$job_id"_entrance.log"
    echo -e "$(cat ./linkis-cg-linkismanager.log ./$yyy_mm/linkis-cg-linkismanager/linkis-log-$yyy_mm* | grep "JobId-$job_id")" >"./"$job_id"_linkismanager.log"
    echo -e "$(cat ./linkis-ps-publicservice.log ./$yyy_mm/linkis-ps-publicservice/linkis-log-$yyy_mm* | grep "JobId-$job_id")" >"./"$job_id"_publicservice.log"
  else
    # 链路日志未滚动
    echo -e "$(cat ./linkis-cg-entrance.log | grep "JobId-$job_id")" >"./"$job_id"_entrance.log"
    echo -e "$(cat ./linkis-cg-linkismanager.log | grep "JobId-$job_id")" >"./"$job_id"_linkismanager.log"
    echo -e "$(cat ./linkis-ps-publicservice.log | grep "JobId-$job_id")" >"./"$job_id"_publicservice.log"
  fi
}

if [ "$6" == "1" ]; then
  remote_link_logs $*
else
  remote_engine_logs $*
fi

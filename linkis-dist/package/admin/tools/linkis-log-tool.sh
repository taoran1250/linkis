#!/bin/bash

server_name=$1
job_id=$2
log_path=$3
option_flag=$4
port_id=$5

if [ ! -d "./$job_id" ]; then
  mkdir ./$job_id
fi

if [ "$option_flag" == "0" ]; then
  # 获取远端ec日志
  ssh hadoop@$server_name 'bash -s' $job_id $log_path $yyy_mm $yyyy_mm_dd "$created_time_date" <./remote-engine.sh

  # 符合远程的ec日志复制到本地
  scp -r hadoop@$server_name:$log_path/"$job_id"_engineconn.log $(pwd)/$job_id/engineconn_"$server_name"_"$port_id".log

  # 删除远端临时日志
  ssh hadoop@$server_name "rm -f $log_path/$2_engineconn.log"
elif [ "$option_flag" == "1" ]; then
  ssh hadoop@$server_name 'bash -s' $job_id $log_path $yyy_mm $yyyy_mm_dd $created_time_date 1 <./remote-engine.sh

  scp -r hadoop@$server_name:$log_path/"$job_id"_entrance.log $(pwd)/$job_id/entrance_"$server_name"_"$port_id".log
  scp -r hadoop@$server_name:$log_path/"$job_id"_linkismanager.log $(pwd)/$job_id/linkismanager_"$server_name"_"$port_id".log
  scp -r hadoop@$server_name:$log_path/"$job_id"_publicservice.log $(pwd)/$job_id/publicservice_"$server_name"_"$port_id".log

  ssh hadoop@$server_name "rm -f $log_path/$2_entrance.log"
  ssh hadoop@$server_name "rm -f $log_path/$2_linkismanager.log"
  ssh hadoop@$server_name "rm -f $log_path/$2_publicservice.log"
else
  echo "暂不支持的日志分析！"
fi

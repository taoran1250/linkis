#!/bin/bash

export PYTHONIOENCODING=utf-8
# 任务id
job_id=$1
current_path=$(pwd)
# 本机ip
local_ip=$(ifconfig -a | grep inet | grep -v 127.0.0.1 | grep -v inet6 | awk '{print $2}' | tr -d "addr:")
# 定义全局历史接口url
export task_list_result_url="http://$local_ip:8088/api/rest_j/v1/jobhistory/list?taskID=$job_id&pageNow=1&pageSize=50&isAdminView=true"

# 引擎常量(用于判断是否已经提交到引擎)
submit_engine_constants="Task submit to ec"
# 日志路径(用于查看ec端日志)
engine_local_log_path="EngineConn local log path"
# 链路日志路径
link_log_path="/data/bdp/logs/linkis"

if [ ! -d "./json" ]; then
  mkdir ./json
fi

# 根据key获取值
function getValueForTasks() {
  echo $(echo $1 | python -c "import sys, json; print (json.load(sys.stdin)['data']['tasks'][0]['$2'])")
}

function getValueForDetail() {
  echo $(echo $1 | python -c "import sys, json; print (json.load(sys.stdin)['data']['$2'])")
}

# 全局历史任务列表接口
function get_job_list_by_id() {
  # 将根据任务id查询出来的结果保存起来
  echo $(curl -s -X GET --header "Token-Code: BML-AUTH" --header "Token-User: hadoop" --header 'Accept: application/json' $task_list_result_url)
}

# 日志明细查询
function get_job_detail_log() {
  log_path=$(getValueForTasks "$1" logPath)
  # 定义openLog接口url
  open_log_url=http://"$local_ip":8088/api/rest_j/v1/filesystem/openLog?path=$log_path
  # 调用openLog接口
  echo $(curl -s -X GET --header "Token-Code: BML-AUTH" --header "Token-User: hadoop" --header 'Accept: application/json' $open_log_url)
}

# -------------------------------------------------------------提供一些帮助查看命令-------------------------------------------------------
help() {
  echo "<-----------------------下面是一些简单命令------------------------------------>"
  echo "NAME"
  echo "    linkis log query tool"
  echo ""
  echo "    linkis-analyze -job jobid info,             查看job 的基础信息 （请求参数 日志目录 等）"
  echo ""
  echo "    linkis-analyze -job jobid info,             查看job相关的日志"
  echo ""
  echo "    linkis-analyze -job jobid ecinfo,           查看job ec日志"
  exit 1
}

# 查看job 的基础信息 （请求参数 日志目录 等）
info() {
  #
  echo "【日志目录说明】"
  echo "【错误日志】$current_path/error/JobId-"$1"_error.log"
  echo "【明细接口返回】$current_path/json/JobId-"$1"_detail.log"
  echo "【链路日志】$current_path/link/JobId-"$1"_entrance.log"
  echo "【链路日志】$current_path/link/JobId-"$1"_linkismanager.log"
  echo "【ec日志】$current_path/ec/JobId-"$1"_engine.log"
}

# 查看job相关的日志
log() {
  echo "【job相关的日志】"
}

# 查看job ec日志
eclog() {
  echo "【ec日志】"
  job_id=$1
  task_list_result_url="http://"$local_ip":8088/api/rest_j/v1/jobhistory/list?taskID=$1&pageNow=1&pageSize=50&isAdminView=false"
  task_list_result_ec=$(echo $(curl -X GET --header "Token-Code: BML-AUTH" --header "Token-User: hadoop" --header 'Accept: application/json' $task_list_result_url))
  instance_ec=$(getValueForTasks "$task_list_result_ec" instance)
  instance_arr_ec=(${instance//:/ })
  open_log_result_ec=$(get_job_detail_log "$task_list_result_ec")
  log_detail_ec=$(getValueForDetail "$open_log_result_ec" log)
  echo -e "$log_detail_ec" >$current_path"/json/JobId-"$job_id"_detail.log"

  local_log_path=$(cat $(pwd)"/json/JobId-$job_id"_detail.log | grep "$engine_local_log_path")
  local_log_path_arr=(${local_log_path//:/ })
  thirdToLastIndex=$((${#local_log_path_arr[@]} - 3))
  server_name=${local_log_path_arr[thirdToLastIndex]}
  lastIndex=$((${#local_log_path_arr[@]} - 1))
  log_path=${local_log_path_arr[lastIndex]}

  echo "【ec服务地址】"$server_name
  echo "【ec日志路径】"$log_path
}

option() {
  while [ -n "$1" ]; do
    case $1 in
    -h)
      help
      break
      ;;
    -help)
      help
      break
      ;;
    -job)
      if [ $3 == "info" ]; then
        info $2
      elif [ $3 == "log" ]; then
        log $2
      elif [ $3 == "eclog" ]; then
        eclog $2
      elif [ $3 == "h" ]; then
        help
      elif [ $3 == "help" ]; then
        help
      else
        echo $3": unknow command"
      fi
      break
      ;;
    *)
      echo $1": unknow option"
      break
      ;;
    esac
  done
}

if [ $# -eq 1 ] && [ $1 == "-help" -o $1 == "-h" ]; then
  option $*
  exit 1
fi

if [ $# -eq 3 ]; then
  option $*
  exit 1
fi

# --------------------------------------------------------------------根据jobid拉取日志到本地-------------------------------------------------------
# 参数检查
function check() {
  if [ $# -ne 1 ]; then
    echo "请输入任务id"
    exit 1
  fi
  # 校验第一个参数合法性，只能是数字
  expr $1 + 0 &>/dev/null
  if [ $? -ne 0 ]; then
    echo "请输入合法的任务id,任务id只能是数字！"
    exit 1
  fi
}

# 检查接口状态
function check_status() {
  code=$(getValueForTasks "$task_list_result" status)
  # 接口返回成功直接返回
  if [[ $code -ne 'Failed' ]]; then
    echo "任务已成功执行，不需要分析日志"
    exit 1
  elif [[ -z $code ]]; then
    echo "不存在的任务，请检查下任务id"
    exit 1
  fi
}

# 获取远程EC日志
function remote_access_to_ec_logs() {
  # 获取local_log_path
  local_log_path=$(cat $(pwd)"/json/$job_id"_detail.json | grep "$engine_local_log_path")
  local_log_path_arr=(${local_log_path//:/ })
  # 获取倒数第三个元素下标
  thirdToLastIndex=$((${#local_log_path_arr[@]} - 3))
  # 服务名
  server_name=${local_log_path_arr[thirdToLastIndex]}
  port_id_s=${local_log_path_arr[$((${#local_log_path_arr[@]} - 2))]}
  port_id=${port_id_s%)*}
  # 最后一个元素下标
  lastIndex=$((${#local_log_path_arr[@]} - 1))
  # 日志地址
  log_path=${local_log_path_arr[lastIndex]}

  # 脚本调用 获取远端ec日志
  source ./linkis-log-tool.sh $server_name $job_id $log_path 0 $port_id

  echo -e "\e[32m 服务日志所在路径：\e[0m"$server_name:$port_id"($log_path)"
  echo -e "\e[32m 获取的完整日志见：\e[0m"$(pwd)/$job_id/engineconn_"$server_name"_"$port_id".log
  error_log=$(cat $(pwd)/$job_id/engineconn_"$server_name"_"$port_id".log | grep "ERROR")
  if [[ $error_log ]]; then
    echo -e "\e[32m 异常日志信息如下：\e[0m"
  fi
  echo -e "\e[31m $(cat $(pwd)/$job_id/engineconn_"$server_name"_"$port_id".log | grep "ERROR") \e[0m"
}

# 获取远端链路日志
function remote_access_to_link_logs() {
  instance=$(getValueForTasks "$task_list_result" instance)
  instance_arr=(${instance//:/ })
  servername=${instance_arr[0]}
  port_id=${instance_arr[1]}

  # 脚本调用 获取远端链路日志
  source ./linkis-log-tool.sh $servername $job_id $link_log_path 1 $port_id

  # entrance
  echo -e "\e[32m 服务日志所在路径：\e[0m"$servername:$port_id"($link_log_path)"
  echo -e "\e[32m 获取的完整日志见：\e[0m"$(pwd)/$job_id/entrance_"$server_name"_"$port_id".log
  error_log=$(cat $(pwd)/$job_id/entrance_"$server_name"_"$port_id".log | grep "ERROR")
  if [[ $error_log ]]; then
    echo -e "\e[32m engineconn异常日志信息如下：\e[0m"
  fi
  echo -e "\e[31m $error_log \e[0m"

  # linkismanager
  echo -e "\e[32m 获取的完整日志见：\e[0m"$(pwd)/$job_id/linkismanager_"$server_name"_"$port_id".log
  error_log=$(cat $(pwd)/$job_id/linkismanager_"$server_name"_"$port_id".log | grep "ERROR")
  if [[ $error_log ]]; then
    echo -e "\e[32m linkismanager异常日志信息如下：\e[0m"
  fi
  echo -e "\e[31m $error_log \e[0m"

  # publicservice
  echo -e "\e[32m 获取的完整日志见：\e[0m"$(pwd)/$job_id/publicservice_"$server_name"_"$port_id".log
  error_log=$(cat $(pwd)/$job_id/publicservice_"$server_name"_"$port_id".log | grep "ERROR")
  if [[ $error_log ]]; then
    echo -e "\e[32m publicservice异常日志信息如下：\e[0m"
  fi
  echo -e "\e[31m $error_log \e[0m"
}

# step1 提示信息
function print_step1_echo() {
  # 查询语句
  execution_code=$(getValueForTasks "$task_list_result" executionCode)
  # 标签
  labels=$(getValueForTasks "$task_list_result" labels)
  labers_array=(${labels//,/ })
  user_creator_str=${labers_array[0]}
  user_creator_s=${user_creator_str:15}
  user_creator=${user_creator_s%\'*}
  engine_type_str=${labers_array[2]}
  engine_type_s=${engine_type_str:13}
  engine_type=${engine_type_s%\']*}
  # 状态
  status=$(getValueForTasks "$task_list_result" status)
  # 已耗时
  cost_time=$(getValueForTasks "$task_list_result" costTime)
  # 创建时间
  yyyy_mm_dd_hh_mm_ss=$(date -d @$created_time_date "+%Y-%m-%d %H:%M:%S")
  # Entrance实例
  instance=$(getValueForTasks "$task_list_result" instance)
  # EC引擎实例
  engine_instance=$(getValueForTasks "$task_list_result" engineInstance)
  # 请求相关配置参数
  echo -e "\e[32m 任务id: \e[0m"$job_id
  echo -e "\e[32m 查询语句: \e[0m"$execution_code
  echo -e "\e[32m 标签: \e[0m"$user_creator","$engine_type
  echo -e "\e[32m 状态: \e[0m"$status
  echo -e "\e[32m 已耗时: \e[0m"$(($cost_time / 1000))
  echo -e "\e[32m 创建时间: \e[0m"$yyyy_mm_dd_hh_mm_ss
  echo -e "\e[32m Entrance实例: \e[0m"$instance
  echo -e "\e[32m EC引擎实例: \e[0m"$engine_instance
  echo -e "\e[32m 请求相关配置参数: \e[0m"
}

# step2 提示信息
function print_step2_echo() {
  echo -e "\e[32m ***【step2 开始分析任务异常日志】***** \e[0m"
  echo -e "\e[31m $(cat $(pwd)"/json/$job_id"_detail.json | grep 'ERROR') \e[0m"
}

function check_task_date() {
  # 获取任务创建时间
  created_time=$(getValueForTasks "$task_list_result" createdTime)

  # 截取时间戳
  export created_time_date=$(echo $created_time | cut -b 1-10)
  export yyyy_mm_dd=$(date -d @$created_time_date "+%Y-%m-%d")
  export yyy_mm=$(date -d @$created_time_date "+%Y-%m")

  # 当前日期与任务创建时间相差不能大于3
  days_between=$((($(date +%s) - $created_time_date) / (24 * 60 * 60)))
  if [ $days_between -gt 3 ]; then
    echo "只支持最近3天任务查询"
    exit 1
  fi
}

# 参数校验
check $*

echo -e "\e[32m ***【Step1 开始获取任务信息】***** \e[0m"

# 任务查询
task_list_result=$(get_job_list_by_id)

# 根据任务列表接口返回判断status
check_status

# 日期校验
check_task_date

# 提示信息
print_step1_echo

open_log_result=$(get_job_detail_log "$task_list_result")
log_detail=$(getValueForDetail "$open_log_result" log)

# 将log日志写入到文件中
echo -e $log_detail >$current_path"/json/"$job_id"_detail.json"

print_step2_echo

if [[ $open_log_result =~ $submit_engine_constants ]]; then
  # 如果log path存在，需要通过任务id拉取EC端的stdout的日志
  if [[ $open_log_result =~ $engine_local_log_path ]]; then
    echo -e "\e[32m ***【step3 尝试分析任务运行的引擎日志】***** \e[0m"
    # 获取远程EC日志
    remote_access_to_ec_logs
  else
    # 获取 task_submit_to_ec_desc
    task_submit_to_ec_desc=$(cat $current_path"/json/"$job_id"_detail.json" | grep "$submit_engine_constants")
    echo -e "\e[32m 请联系Linkis 运维，EC的信息是 \e[0m"$task_submit_to_ec_desc
  fi
else
  echo -e "\e[32m ***【step3 开始分析 链路服务日志 】***** \e[0m"
  # 获取链路日志
  remote_access_to_link_logs
fi

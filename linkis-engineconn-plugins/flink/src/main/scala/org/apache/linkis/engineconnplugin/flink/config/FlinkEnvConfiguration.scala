/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.flink.config

import org.apache.linkis.common.conf.{CommonVars, TimeType}
import org.apache.linkis.engineconnplugin.flink.client.config.entries.ExecutionEntry
import org.apache.linkis.engineconnplugin.flink.constants.FlinkECConstant

object FlinkEnvConfiguration {

  val FLINK_HOME_ENV = "FLINK_HOME"
  val FLINK_CONF_DIR_ENV = "FLINK_CONF_DIR"
  val FLINK_VERSION = CommonVars("flink.version", "1.12.2")

  val FLINK_HOME =
    CommonVars("flink.home", CommonVars(FLINK_HOME_ENV, "/appcom/Install/flink").getValue)

  val FLINK_CONF_DIR = CommonVars(
    "flink.conf.dir",
    CommonVars(FLINK_CONF_DIR_ENV, "/appcom/config/flink-config").getValue
  )

  val FLINK_DIST_JAR_PATH = CommonVars(
    "flink.dist.jar.path",
    FLINK_HOME.getValue + s"/lib/flink-dist_2.11-${FLINK_VERSION.getValue}.jar"
  )

  val FLINK_CONF_YAML = CommonVars("flink.conf.yaml.dir", "flink-conf.yaml")

  val FLINK_PROVIDED_LIB_PATH = CommonVars("flink.lib.path", "")

  val FLINK_PROVIDED_USER_LIB_PATH =
    CommonVars("flink.user.lib.path", "", "The hdfs lib path of each user in Flink EngineConn.")

  val FLINK_LIB_LOCAL_PATH = CommonVars(
    "flink.local.lib.path",
    "/appcom/Install/flink/lib",
    "The local lib path of Flink EngineConn."
  )

  val FLINK_USER_LIB_LOCAL_PATH = CommonVars(
    "flink.user.local.lib.path",
    "/appcom/Install/flink/lib",
    "The local lib path of each user in Flink EngineConn."
  )

  val FLINK_YAML_MERGE_ENABLE = CommonVars("flink.yaml.merge.enable", true)
  val FLINK_SHIP_DIRECTORIES = CommonVars("flink.yarn.ship-directories", "")
  val FLINK_SHIP_REMOTE_DIRECTORIES = CommonVars("flink.yarn.remote.ship-directories", "")

  val FLINK_CHECK_POINT_ENABLE = CommonVars("flink.app.checkpoint.enable", false)
  val FLINK_CHECK_POINT_INTERVAL = CommonVars("flink.app.checkpoint.interval", 3000)
  val FLINK_CHECK_POINT_MODE = CommonVars("flink.app.checkpoint.mode", "EXACTLY_ONCE")
  val FLINK_CHECK_POINT_TIMEOUT = CommonVars("flink.app.checkpoint.timeout", 60000)

  val FLINK_CHECK_POINT_MIN_PAUSE =
    CommonVars("flink.app.checkpoint.minPause", FLINK_CHECK_POINT_INTERVAL.getValue)

  val FLINK_SAVE_POINT_PATH = CommonVars("flink.app.savePointPath", "")

  val FLINK_APP_ALLOW_NON_RESTORED_STATUS =
    CommonVars("flink.app.allowNonRestoredStatus", "false")

  val FLINK_SQL_PLANNER =
    CommonVars("flink.sql.planner", ExecutionEntry.EXECUTION_PLANNER_VALUE_BLINK)

  val FLINK_SQL_EXECUTION_TYPE =
    CommonVars("flink.sql.executionType", ExecutionEntry.EXECUTION_TYPE_VALUE_STREAMING)

  val FLINK_SQL_DEV_SELECT_MAX_LINES = CommonVars("flink.dev.sql.select.lines.max", 500)

  val FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME =
    CommonVars("flink.dev.sql.result.wait.time.max", new TimeType("1m"))

  val LINKIS_FLINK_LOG4J_CHECK_ENABLE = CommonVars("linkis.flink.log4j.check.enable", true)
  val LINKIS_FLINK_LOG4J_CHECK_KEYWORDS = CommonVars("linkis.flink.log4j.check.keywords", "")
  val FLINK_APPLICATION_ARGS = CommonVars("flink.app.args", "")
  val FLINK_APPLICATION_MAIN_CLASS = CommonVars("flink.app.main.class", "")
  val FLINK_APPLICATION_MAIN_CLASS_JAR = CommonVars("flink.app.main.class.jar", "")
  val FLINK_APPLICATION_CLASSPATH = CommonVars("flink.app.user.class.path", "")

  val FLINK_CLIENT_REQUEST_TIMEOUT =
    CommonVars("flink.client.request.timeout", new TimeType("30s"))

  val FLINK_ONCE_APP_STATUS_FETCH_INTERVAL =
    CommonVars("flink.app.fetch.status.interval", new TimeType("5s"))

  val FLINK_ONCE_JAR_APP_REPORT_APPLICATIONID_INTERVAL =
    CommonVars("flink.app.report.appid.interval", new TimeType("60s"))

  val FLINK_ONCE_APP_STATUS_FETCH_FAILED_MAX = CommonVars("flink.app.fetch.status.failed.num", 3)

  val FLINK_REPORTER_ENABLE = CommonVars("linkis.flink.reporter.enable", false)
  val FLINK_REPORTER_CLASS = CommonVars("linkis.flink.reporter.class", "")
  val FLINK_REPORTER_INTERVAL = CommonVars("linkis.flink.reporter.interval", new TimeType("60s"))

  val FLINK_EXECUTION_ATTACHED = CommonVars("linkis.flink.execution.attached", true)
  val FLINK_CONFIG_PREFIX = "_FLINK_CONFIG_."

  val FLINK_KERBEROS_ENABLE = CommonVars("linkis.flink.kerberos.enable", false)

  val FLINK_KERBEROS_LOGIN_CONTEXTS =
    CommonVars("linkis.flink.kerberos.login.contexts", "Client,KafkaClient")

  val FLINK_KERBEROS_LOGIN_KEYTAB = CommonVars("linkis.flink.kerberos.login.keytab", "")
  val FLINK_KERBEROS_LOGIN_PRINCIPAL = CommonVars("linkis.flink.kerberos.login.principal", "")
  val FLINK_KERBEROS_CONF_PATH = CommonVars("linkis.flink.kerberos.krb5-conf.path", "")

  val FLINK_PARAMS_BLANK_PLACEHOLER =
    CommonVars("linkis.flink.params.placeholder.blank", "\u0001")

  val FLINK_MANAGER_MODE_CONFIG_KEY = CommonVars("linkis.flink.manager.mode.on", false)

  val FLINK_MANAGER_LOAD_TASK_MAX = CommonVars("linkis.flink.manager.load.task.max", 50)

  val HADOOP_CONF_DIR = CommonVars("linkis.flink.hadoop.conf.dir", System.getenv("HADOOP_CONF_DIR"))

  val FLINK_MANAGER_CLIENT_MAX_NUM = CommonVars("linkis.flink.client.num.max", 200)

  val FLINK_MANAGER_CLIENT_EXPIRE_MILLS =
    CommonVars("linkis.flink.client.expire.mills", 3600 * 1000)

  // default NONE
  val FLINK_MANAGER_HIGHAVAILABLE_MODE =
    CommonVars("linkis.flink.manager.highavailable.mode", FlinkECConstant.HIGH_AVAILABLE_MODE_NONE)

  val FLINK_HANDSHAKE_WAIT_TIME_MILLS =
    CommonVars("linkis.flink.handshake.wait.time.mills", 60 * 1000)

  val FLINK_ENV_JAVA_OPTS =
    CommonVars("flink.env.java.opts", "env.java.opts")

  val FLINK_CONSTANT_CONFIGURATION =
    CommonVars("flink.constant.configuration", "log4j.properties")

}

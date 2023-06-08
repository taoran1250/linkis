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

package org.apache.linkis.instance.label.service.conf

import org.apache.linkis.common.conf.Configuration

object InstanceConfigration {

  val GOVERNANCE_STATION_ADMIN = Configuration.GOVERNANCE_STATION_ADMIN

  val QUEUE = "queue"

  val EC_CLIENT_TYPE_ATTACH = "attach"

  val EC_CLIENT_TYPE_DETACH = "detach"

  val YARN_APPID_NAME_KEY = "applicationId"

  val YARN_APP_URL_KEY = "applicationUrl"

  val YARN_APP_NAME_KEY = "appicationName"

  val YARN_MODE_KEY = "yarnMode"

  val EC_SERVICE_INSTANCE_KEY = "serviceInstance"

  val ECM_SERVICE_INSTANCE_KEY = "ecmServiceInstance"

  val MANAGER_SERVICE_INSTANCE_KEY = "managerServiceInstance"

  val NODE_STATUS_KEY = "nodeStatus"

  val EC_LAST_UNLOCK_TIMESTAMP = "lastUnlockTimestamp"

  val YARN_APP_TYPE_LIST_KEY = "yarnAppTypeList"

  val YARN_APP_STATE_LIST_KEY = "yarnAppStateList"

  val YARN_APP_TYPE_KEY = "yarnAppType"

  val YARN_APP_TYPE_SPARK = "spark"

  val YARN_APP_TYPE_FLINK = "flink"

  val EC_OPERATE_LIST = "list"

  val EC_OPERATE_STATUS = "status"

  val YARN_APP_RESULT_LIST_KEY = "yarnAppResultList"
}

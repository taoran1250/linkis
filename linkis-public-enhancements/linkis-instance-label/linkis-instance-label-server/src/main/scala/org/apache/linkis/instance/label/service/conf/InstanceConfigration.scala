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

  val EC_FLINK_CLIENT_TYPE_ATTACH = "attach"

  val EC_FLINK_CLIENT_TYPE_DETACH = "detach"

  val YARN_APPID_NAME_KEY = "appicationId"

  val YARN_APP_URL_KEY = "applicationUrl"

  val YARN_MODE_KEY = "yarnMode"

  val EC_SERVICE_INSTANCE_KEY = "serviceInstance"

  val ECM_SERVICE_INSTANCE_KEY = "ecmServiceInstance"

  val NODE_STATUS_KEY = "nodeStatus"
}

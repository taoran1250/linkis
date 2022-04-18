/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.governance.common.utils

import org.apache.linkis.governance.common.constant.job.JobRequestConstants.NULL_ID
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan

import java.util

object SkywalkingTraceUtil {

  def addTagForActiveSpan(map: util.Map[String, Object], tagKey: String): Unit = {
    val id = getValue(map, tagKey)
    ActiveSpan.tag(tagKey, id)
  }

  def addTagForActiveSpan(tagKey: String, tagValue: String): Unit = {
    ActiveSpan.tag(tagKey, tagValue)
  }

  def getValue(map: util.Map[String, Object], key: String): String = {
    if (map != null) {
      if (map.containsKey(key)) {
        if (null != map.get(key)) {
          return map.get(key).toString
        }
      }
    }
    NULL_ID
  }

}

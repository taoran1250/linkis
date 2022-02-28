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

package org.apache.linkis.metadata.util;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.CommonVars$;

public class DWSConfig {

    public static CommonVars<String> HIVE_CONF_DIR =
            CommonVars$.MODULE$.apply(
                    "hive.config.dir",
                    CommonVars$.MODULE$
                            .apply("HIVE_CONF_DIR", "/appcom/config/hadoop-config")
                            .getValue());
    public static CommonVars<String> HIVE_META_URL = CommonVars$.MODULE$.apply("hive.meta.url", "");
    public static CommonVars<String> HIVE_META_USER =
            CommonVars$.MODULE$.apply("hive.meta.user", "");
    public static CommonVars<String> HIVE_META_PASSWORD =
            CommonVars$.MODULE$.apply("hive.meta.password", "");

    // wds.linkis.metadata.hive.encode.enable配置HIVE BASE64加解密
    public static final CommonVars<Boolean> HIVE_PASS_ENCODE_ENABLED =
            CommonVars.apply("wds.linkis.metadata.hive.encode.enabled", new Boolean(false));

    public static CommonVars<Boolean> HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED =
            CommonVars$.MODULE$.apply(
                    "linkis.metadata.hive.permission.with-login-user-enabled", true);
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.computation.executor.hook

/**
 * The PythonModuleLoad class is designed to load Python modules into the
 * execution environment dynamically. This class is not an extension of UDFLoad,
 * but shares a similar philosophy of handling dynamic module loading based on
 * user preferences and system configurations.
 */
class PythonModuleLoad {

  /** A client for interacting with the BML (Big Model Library) service for remote resource loading */
  protected val bmlClient: BmlClient = BmlClientFactory.createBmlClient()

  /** Abstract properties to be defined by the subclass */
  protected var udfType: String = _
  protected var category: String = _
  protected var runType: String = _

  /**
   * Read a file's content from the local file system.
   * If the file does not exist, log a warning and return an empty string.
   * 
   * @param filePath The path of the file to be read.
   * @return The content of the file if it exists, otherwise an empty string.
   */
  protected def readFile(filePath: String): String = {
    try {
      val file = new File(filePath)
      if (file.exists()) {
        val source = Source.fromFile(file)
        val content = source.getLines().mkString("\n")
        source.close()
        content
      } else {
        Logger.warn(s"File not found: $filePath")
        ""
      }
    } catch {
      case e: Exception => 
        Logger.error(s"Failed to read file: $filePath", e)
        ""
    }
  }

  /**
   * Read a file's content from the BML service.
   * 
   * @param user The user for whom the resource should be fetched.
   * @param resourceId The ID of the resource to fetch.
   * @param resourceVersion The version of the resource to fetch.
   * @return The content of the file if fetched successfully, otherwise an empty string.
   */
  protected def readFile(user: String, resourceId: String, resourceVersion: String): String = {
    try {
      bmlClient.downloadResource(user, resourceId, resourceVersion)
    } catch {
      case e: Exception => 
        Logger.error(s"Failed to download resource: $resourceId", e)
        ""
    }
  }

  /**
   * Generate and execute the code necessary for loading Python modules.
   * 
   * @param executor An object capable of executing code in the current engine context.
   */
  protected def loadModules(executor: Executor): Unit = {
    val engineCreationContext =
      EngineConnManager.getEngineConnManager.getEngineConn.getEngineCreationContext
    val user = engineCreationContext.getUser
    val loadAllModules =
      engineCreationContext.getOptions.getOrElse("linkis.user.module.all.load", "true").toBoolean
    val customModuleIdsStr = 
      engineCreationContext.getOptions.getOrElse("linkis.user.module.custom.ids", "")
    val customModuleIds = customModuleIdsStr.split(",").filterNot(_.isEmpty).map(_.trim.toLong)

    Logger.info(s"start loading modules, user: $user, load all: $loadAllModules, moduleIds: ${customModuleIds.mkString("[", ",", "]")}")

    val modulesToLoad = if (loadAllModules) {
      PythonModuleClient.getAllModulesForUser(user, category, udfType)
    } else {
      PythonModuleClient.getModulesByIds(user, customModuleIds, category, udfType)
    }

    val codeBuffer = modulesToLoad.flatMap { module =>
      val code = constructModuleCode(module)
      if (code.nonEmpty) code.split("\n").filterNot(_.isEmpty) else Array.empty[String]
    }

    executeFunctionCode(codeBuffer.toArray, executor)
  }

  /**
   * Abstract method to construct the code for loading a Python module.
   * 
   * @param moduleInfo Information about the Python module to be loaded.
   * @return A code string that represents the necessary operations to load the module.
   */
  protected def constructModuleCode(moduleInfo: ModuleInfoVo): String
}

// Note: The actual implementation of methods like `executeFunctionCode` and `construct

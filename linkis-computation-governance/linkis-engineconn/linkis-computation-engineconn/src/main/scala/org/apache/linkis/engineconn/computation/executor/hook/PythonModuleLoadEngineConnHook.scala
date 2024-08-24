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

import org.slf4j.{Logger, Logging}
import com.yourcompany.yourpackage.labels.{CodeLanguageLabel, EngineTypeLabel}
import com.yourcompany.yourpackage.engine.{EngineConn, EngineCreationContext}



object PythonModuleLoadEngineConnHook extends PythonModuleLoad with EngineConnHook with Logging {

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    val codeLanguageLabel = new CodeLanguageLabel
    engineCreationContext.getLabels().asScala.find(_.isInstanceOf[EngineTypeLabel]) match {
      case Some(engineTypeLabel) =>
        codeLanguageLabel.setCodeType(
          getRealRunType(engineTypeLabel.asInstanceOf[EngineTypeLabel].getEngineType).toString
        )
      case None =>
        codeLanguageLabel.setCodeType("Python") // Assuming "Python" is the default code type
        logger.warn("no EngineTypeLabel found, use default runType 'Python'")
    }
    val labels = Array[Label[_]](codeLanguageLabel)
    loadPythonModule(labels)
  }

  override def afterEngineServerStartFailed(engineCreationContext: EngineCreationContext, throwable: Throwable): Unit = {
    logger.error(s"Failed to start Engine Server: ${throwable.getMessage}", throwable)
  }

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    logger.info("Preparing to create Python Engine Connection...")
  }

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    logger.info(s"Before executing command on Python Engine Connection: ${engineConn}")
  }

  // Implementation of PythonModuleLoad
  override def loadPythonModule(labels: Array[Label[_]]): Unit = {
    // Code to load Python modules based on the labels
    // This is a placeholder for actual implementation
    logger.info(s"Loading Python Modules with labels: ${labels.mkString(", ")}")
  }
}

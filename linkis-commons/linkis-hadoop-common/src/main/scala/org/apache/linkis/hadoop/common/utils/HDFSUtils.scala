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

package org.apache.linkis.hadoop.common.utils

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.hadoop.common.conf.HadoopConf._
import org.apache.linkis.hadoop.common.entity.HDFSFileSystemContainer
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.UserGroupInformation

import java.io.File
import java.nio.file.Paths
import java.security.PrivilegedExceptionAction
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import scala.collection.JavaConverters._

object HDFSUtils extends Logging {

  private val fileSystemCache: java.util.Map[String, HDFSFileSystemContainer] =
    new ConcurrentHashMap[String, HDFSFileSystemContainer]()

  private val LOCKER_SUFFIX = "_HDFS"

  private val count = new AtomicLong

  /**
   * For FS opened with public tenants, we should not perform close action, but should close only
   * when hdfsfilesystem encounters closed problem
   * 对于使用公共租户开启的FS，我们不应该去执行close动作，应该由hdfsfilesystem遇到closed问题时才进行关闭
   */
  if (HadoopConf.HDFS_ENABLE_CACHE && HadoopConf.HDFS_ENABLE_CACHE_CLOSE) {
    logger.info("HDFS Cache clear enabled ")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = Utils.tryAndWarn {
          fileSystemCache
            .values()
            .asScala
            .filter { hdfsFileSystemContainer =>
              hdfsFileSystemContainer.canRemove() && StringUtils.isNotBlank(
                hdfsFileSystemContainer.getUser
              )
            }
            .foreach { hdfsFileSystemContainer =>
              val locker = hdfsFileSystemContainer.getUser + LOCKER_SUFFIX
              locker.intern() synchronized {
                if (hdfsFileSystemContainer.canRemove()) {
                  fileSystemCache.remove(hdfsFileSystemContainer.getUser)
                  IOUtils.closeQuietly(hdfsFileSystemContainer.getFileSystem)
                  logger.info(
                    s"user${hdfsFileSystemContainer.getUser} to remove hdfsFileSystemContainer,because hdfsFileSystemContainer can remove"
                  )
                }
              }
            }
        }
      },
      3 * 60 * 1000,
      60 * 1000,
      TimeUnit.MILLISECONDS
    )
  }

  def getConfiguration(user: String): Configuration = getConfiguration(user, hadoopConfDir)

  def getConfigurationByLabel(user: String, label: String): Configuration = {
    getConfiguration(user, getHadoopConDirByLabel(label))
  }

  private def getHadoopConDirByLabel(label: String): String = {
    if (StringUtils.isBlank(label)) {
      hadoopConfDir
    } else {
      val prefix = if (HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue.endsWith("/")) {
        HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue
      } else {
        HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue + "/"
      }
      prefix + label
    }
  }

  def getConfiguration(user: String, hadoopConfDir: String): Configuration = {
    val confPath = new File(hadoopConfDir)
    if (!confPath.exists() || confPath.isFile) {
      throw new RuntimeException(
        s"Create hadoop configuration failed, path $hadoopConfDir not exists."
      )
    }
    val conf = new Configuration()
    conf.addResource(
      new Path(Paths.get(hadoopConfDir, "core-site.xml").toAbsolutePath.toFile.getAbsolutePath)
    )
    conf.addResource(
      new Path(Paths.get(hadoopConfDir, "hdfs-site.xml").toAbsolutePath.toFile.getAbsolutePath)
    )
    conf
  }

  def getHDFSRootUserFileSystem: FileSystem = getHDFSRootUserFileSystem(
    getConfiguration(HADOOP_ROOT_USER.getValue)
  )

  def getHDFSRootUserFileSystem(conf: org.apache.hadoop.conf.Configuration): FileSystem =
    getHDFSUserFileSystem(HADOOP_ROOT_USER.getValue, conf)

  /**
   * If the cache switch is turned on, fs will be obtained from the cache first
   * @param userName
   * @return
   */
  def getHDFSUserFileSystem(userName: String): FileSystem = {
    if (HadoopConf.HDFS_ENABLE_CACHE) {
      val locker = userName + LOCKER_SUFFIX
      locker.intern().synchronized {
        if (fileSystemCache.containsKey(userName)) {
          val hdfsFileSystemContainer = fileSystemCache.get(userName)
          hdfsFileSystemContainer.addAccessCount()
          hdfsFileSystemContainer.updateLastAccessTime
          hdfsFileSystemContainer.getFileSystem
        } else {
          getHDFSUserFileSystem(userName, getConfiguration(userName))
        }
      }
    } else {
      getHDFSUserFileSystem(userName, getConfiguration(userName))
    }
  }

  def getHDFSUserFileSystem(
      userName: String,
      conf: org.apache.hadoop.conf.Configuration
  ): FileSystem = {

    if (HadoopConf.FS_CACHE_DISABLE.getValue && null != conf) {
      conf.set("fs.hdfs.impl.disable.cache", "true")
    }
    if (HadoopConf.HDFS_ENABLE_CACHE) {
      val locker = userName + LOCKER_SUFFIX
      locker.intern().synchronized {
        val hdfsFileSystemContainer = if (fileSystemCache.containsKey(userName)) {
          fileSystemCache.get(userName)
        } else {
          val newHDFSFileSystemContainer =
            new HDFSFileSystemContainer(createFileSystem(userName, conf), userName)
          fileSystemCache.put(userName, newHDFSFileSystemContainer)
          newHDFSFileSystemContainer
        }
        hdfsFileSystemContainer.addAccessCount()
        hdfsFileSystemContainer.updateLastAccessTime
        hdfsFileSystemContainer.getFileSystem
      }
    } else {
      createFileSystem(userName, conf)
    }
  }

  def createFileSystem(userName: String, conf: org.apache.hadoop.conf.Configuration): FileSystem = {
    val createCount = count.getAndIncrement()
    logger.info(s"user ${userName} to create Fs, create time ${createCount}")
    getUserGroupInformation(userName)
      .doAs(new PrivilegedExceptionAction[FileSystem] {
        def run: FileSystem = FileSystem.newInstance(conf)
      })
  }

  def closeHDFSFIleSystem(fileSystem: FileSystem, userName: String): Unit =
    if (null != fileSystem && StringUtils.isNotBlank(userName)) {
      closeHDFSFIleSystem(fileSystem, userName, false)
    }

  def closeHDFSFIleSystem(fileSystem: FileSystem, userName: String, isForce: Boolean): Unit =
    if (null != fileSystem && StringUtils.isNotBlank(userName)) {
      val locker = userName + LOCKER_SUFFIX
      if (HadoopConf.HDFS_ENABLE_CACHE) locker.intern().synchronized {
        val hdfsFileSystemContainer = fileSystemCache.get(userName)
        if (null != hdfsFileSystemContainer) {
          if (isForce) {
            fileSystemCache.remove(hdfsFileSystemContainer.getUser)
            IOUtils.closeQuietly(hdfsFileSystemContainer.getFileSystem)
            logger.info(
              s"user${hdfsFileSystemContainer.getUser} to Force remove hdfsFileSystemContainer"
            )
          } else {
            hdfsFileSystemContainer.minusAccessCount()
          }
        }
      }
      else {
        IOUtils.closeQuietly(fileSystem)
      }
    }

  def getUserGroupInformation(userName: String): UserGroupInformation = {
    if (KERBEROS_ENABLE.getValue) {
      if (!KEYTAB_PROXYUSER_ENABLED.getValue) {
        val path = new File(KEYTAB_FILE.getValue, userName + ".keytab").getPath
        val user = getKerberosUser(userName)
        UserGroupInformation.setConfiguration(getConfiguration(userName))
        UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
      } else {
        val superUser = KEYTAB_PROXYUSER_SUPERUSER.getValue
        val path = new File(KEYTAB_FILE.getValue, superUser + ".keytab").getPath
        val user = getKerberosUser(superUser)
        UserGroupInformation.setConfiguration(getConfiguration(superUser))
        UserGroupInformation.createProxyUser(
          userName,
          UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
        )
      }
    } else {
      UserGroupInformation.createRemoteUser(userName)
    }
  }

  def getKerberosUser(userName: String): String = {
    var user = userName
    if (KEYTAB_HOST_ENABLED.getValue) {
      user = user + "/" + KEYTAB_HOST.getValue
    }
    user
  }

}

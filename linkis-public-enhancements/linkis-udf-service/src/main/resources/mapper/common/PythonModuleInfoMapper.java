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
package org.apache.linkis.udf.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.linkis.udf.entity.PythonModuleInfo;

import java.util.List;

@Mapper
public interface PythonModuleInfoMapper {

    // SQL 1: 模糊查询
    @Select("SELECT * FROM linkis_ps_python_module_info " +
            "WHERE name LIKE CONCAT('%', #{name}, '%') " +
            "AND create_user LIKE CONCAT('%', #{createUser}, '%') " +
            "AND engine_type = #{engineType} " +
            "AND is_expire = #{isExpire} " +
            "AND is_load = #{isLoad} " +
            "ORDER BY create_time DESC")
    List<PythonModuleInfo> selectByConditions(PythonModuleInfo pythonModuleInfo);

    // SQL 2: 更新
    @Update("UPDATE linkis_ps_python_module_info " +
            "<set> " +
            "<if test=\"name != null\">name = #{name},</if> " +
            "<if test=\"description != null\">description = #{description},</if> " +
            "<if test=\"path != null\">path = #{path},</if> " +
            "<if test=\"engineType != null\">engine_type = #{engineType},</if> " +
            "<if test=\"createUser != null\">create_user = #{createUser},</if> " +
            "<if test=\"updateUser != null\">update_user = #{updateUser},</if> " +
            "<if test=\"isLoad != null\">is_load = #{isLoad},</if> " +
            "<if test=\"isExpire != null\">is_expire = #{isExpire},</if> " +
            "<if test=\"createTime != null\">create_time = #{createTime},</if> " +
            "<if test=\"updateTime != null\">update_time = #{updateTime},</if> " +
            "</set> " +
            "WHERE id = #{id}")
    int updatePythonModuleInfo(PythonModuleInfo pythonModuleInfo);

    // SQL 3: 新增
    @Insert("INSERT INTO linkis_ps_python_module_info " +
            "(name, description, path, engine_type, create_user, update_user, is_load, is_expire, create_time, update_time) " +
            "VALUES " +
            "(#{name}, #{description}, #{path}, #{engineType}, #{createUser}, #{updateUser}, #{isLoad}, #{isExpire}, #{createTime}, #{updateTime})")
    int insertPythonModuleInfo(PythonModuleInfo pythonModuleInfo);

    // SQL 4: 带有<if>判断的查询
    @Select("SELECT * FROM linkis_ps_python_module_info " +
            "<where> " +
            "<if test=\"createUser != null\">create_user = #{createUser},</if> " +
            "<if test=\"name != null\">name = #{name},</if> " +
            "<if test=\"id != null\">id = #{id},</if> " +
            "</where>")
    PythonModuleInfo selectByUserAndNameAndId(PythonModuleInfo pythonModuleInfo);

    // SQL 5: 查询包含多个引擎类型的hdfs路径
    @Select("SELECT path FROM linkis_ps_python_module_info " +
            "WHERE create_user = #{username} " +
            "AND engine_type IN " +
            "<foreach item=\"engineType\" index=\"index\" collection=\"enginetypes\" open=\"(\" separator=\",\" close=\")\">" +
            "#{engineType}" +
            "</foreach> " +
            "AND is_expire = 0 " +
            "AND is_load = 1")
    List<String> selectPathsByUsernameAndEnginetypes(String username, List<String> enginetypes);
}

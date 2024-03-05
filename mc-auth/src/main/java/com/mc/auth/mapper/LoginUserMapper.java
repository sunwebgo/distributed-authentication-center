package com.mc.auth.mapper;

import com.mc.common.entity.table.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoginUserMapper {
    User getUserByUsername(@Param("username") String username);

    List<Integer> getUserRole(@Param("id") Long id);

    List<Integer> getRoleList();

    List<String> getRolePermission(@Param("roleId") Integer roleId);

}

package com.mc.common.dubbo;

import com.mc.common.entity.dto.user.UpdatePasswordDTO;
import com.mc.common.entity.dto.user.UserInfoDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.User;
import com.mc.common.entity.to.user.UserTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserServiceInterface {

    CompletableFuture<ResponseResult<User>> getUserByUsername(String username);

    CompletableFuture<ResponseResult<List<Integer>>> getUserRole(Long id);

    CompletableFuture<ResponseResult> checkUsername(Long id, String username);

    CompletableFuture<ResponseResult> checkPhone(String phone);

    CompletableFuture<ResponseResult> register(String username, String password, String phone);

    CompletableFuture<ResponseResult<Map<Integer, List<String>>>> getRolePermissionList();

    CompletableFuture<ResponseResult> updatePassword(UpdatePasswordDTO updatePasswordDTO);

    CompletableFuture<ResponseResult> updateUserInfo(UserInfoDTO userInfoDTO);

    CompletableFuture<ResponseResult> updateAvatar(Long id, String avatarUrl);

    CompletableFuture<ResponseResult<User>> getUser(Long id);

    CompletableFuture<ResponseResult<UserTO>> userIsExist(Long id);

}

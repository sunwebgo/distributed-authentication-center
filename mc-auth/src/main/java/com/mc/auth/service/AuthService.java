package com.mc.auth.service;

import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import com.mc.auth.controller.AuthController;
import com.mc.auth.entity.RegisterUser;
import com.mc.common.entity.dto.user.UpdatePasswordDTO;
import com.mc.common.entity.dto.user.UserInfoDTO;
import com.mc.common.entity.response.ResponseResult;

public interface AuthService {
    ResponseResult register(RegisterUser registerUser);

    ResponseResult revoke(String token);

    ResponseResult checkUsername(Long id, String username);

    ResponseResult checkPhone(String phone);

    ResponseResult checkCaptcha(String phone, String captcha);

    CaptchaResponse<ImageCaptchaVO> createCaptcha(String type);

    ApiResponse<?> checkCaptcha(AuthController.Data data);

    ResponseResult updatePassword(UpdatePasswordDTO updatePasswordDTO);

    ResponseResult updateUserInfo(UserInfoDTO userInfoDTO);

    ResponseResult updateAvatar(Long id, String avatarUrl);
}

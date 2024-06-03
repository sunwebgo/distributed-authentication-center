package com.mc.auth.service.impl;

import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import com.mc.auth.controller.AuthController;
import com.mc.auth.entity.RegisterUser;
import com.mc.auth.entity.TokenEvidence;
import com.mc.auth.interceptor.OAuthNewTokenInterceptor;
import com.mc.auth.service.AuthService;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.NumberConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.dto.user.UpdatePasswordDTO;
import com.mc.common.entity.dto.user.UserInfoDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import com.mc.common.utils.RedisUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class AuthServiceImpl implements AuthService {


    @Resource
    private ConsumerTokenServices consumerTokenServices;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @DubboReference(timeout = 2000)
    private UserServiceInterface userServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * 用户注册
     *
     * @param registerUser
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult register(RegisterUser registerUser) {
        // 判断密码是否一致
        if (!registerUser.getPassword().equals(registerUser.getRePassword())) {
            return ResponseResult.error(Http.PASSWORD_NOT_SAME.getMessage());
        }
        // 判断用户是否已经注册
        ResponseResult checkedUsername = this.checkUsername(null, registerUser.getUsername());
        ResponseResult checkedPhone = this.checkPhone(registerUser.getPhone());
        if (!Objects.equals(checkedUsername.getCode(), CommonConstants.SUCCESS_CODE)
                || !Objects.equals(checkedPhone.getCode(), CommonConstants.SUCCESS_CODE)) {
            return ResponseResult.error(Http.USER_EXIST.getMessage());
        }
        // 判断验证码是否正确（有效）
        Long expireTime = null;
        boolean sendFlag = true;
        try {
            expireTime = RedisUtil.getExpire(CacheConstants.PHONE_CAPTCHA + registerUser.getPhone());
        } catch (Exception e) {
            sendFlag = false;
        }
        // 如果缓存不存在(即还没有发送验证码或者验证码已经过期)
        if ((ObjectUtils.isNotEmpty(expireTime) && Objects.equals(expireTime, NumberConstants.EXPIRE_TIME_STATUS))
                || !sendFlag) {
            return ResponseResult.error(Http.CAPTCHA_ERROR.getMessage());
        }
        //用户注册
        userServiceInterface.register(
                        registerUser.getUsername(),
                        bCryptPasswordEncoder.encode(registerUser.getPassword()),
                        registerUser.getPhone())
                .whenCompleteAsync((result, throwable) -> {
                    if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                        throw new RuntimeException(result.getMessage());
                    }
                }, threadPoolExecutor);
        return ResponseResult.success(Http.REGISTER_SUCCESS.getMessage());
    }

    /**
     * 用户注销
     *
     * @param token
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult revoke(String token) {
        boolean logoutFlag = true;
        try {
            // 从线程局部变量中获取token
            TokenEvidence tokenEvidence = OAuthNewTokenInterceptor.tokenThreadLocal.get();
            if (Boolean.TRUE.equals(tokenEvidence.getIsRefresh())) {
                logoutFlag = consumerTokenServices.revokeToken(tokenEvidence.getToken());
            } else {
                // 请求参数token和请求头中的token不一样的情况
                if (!token.equals(tokenEvidence.getToken())) {
                    logoutFlag = false;
                } else {
                    logoutFlag = consumerTokenServices.revokeToken(token);
                }
            }
        } catch (Exception e) {
            return ResponseResult.error(Http.REVOKE_FAIL.getMessage());
        } finally {
            // 移除线程局部变量,防止内存泄漏
            OAuthNewTokenInterceptor.tokenThreadLocal.remove();
        }
        if (!logoutFlag) {
            return ResponseResult.error(Http.REVOKE_FAIL.getMessage());
        }
        return ResponseResult.success(Http.REVOKE_SUCCESS.getMessage());
    }


    /**
     * 校验用户名是否已经注册
     *
     * @param id
     * @param username
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult checkUsername(Long id, String username) {
        CompletableFuture<ResponseResult> response = userServiceInterface.checkUsername(id, username).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        ResponseResult responseResult = null;
        try {
            responseResult = response.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseResult;
    }

    /**
     * 校验手机号是否已经注册
     *
     * @param phone
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult checkPhone(String phone) {
        CompletableFuture<ResponseResult> response = userServiceInterface.checkPhone(phone).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        ResponseResult responseResult = null;
        try {
            responseResult = response.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseResult;
    }

    /**
     * 校验手机号验证码是否正确
     *
     * @param phone
     * @param captcha
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult checkCaptcha(String phone, String captcha) {
        String captchaCache = RedisUtil.get(CacheConstants.PHONE_CAPTCHA + phone);
        if (!StringUtils.equals(captcha, captchaCache) || StringUtils.isBlank(captchaCache)) {
            return ResponseResult.error(Http.CAPTCHA_ERROR.getMessage());
        }
        return ResponseResult.success();
    }

    @Resource
    private ImageCaptchaApplication imageCaptchaApplication;

    /**
     * 生成验证码
     *
     * @return {@link ImageCaptchaResourceManager}
     */
    @Override
    public CaptchaResponse<ImageCaptchaVO> createCaptcha(String type) {
        return imageCaptchaApplication.generateCaptcha(type);
    }

    /**
     * 校验验证码
     *
     * @param data
     * @return {@link ApiResponse}<{@link ?}>
     */
    @Override
    public ApiResponse<?> checkCaptcha(AuthController.Data data) {
        ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(), data.getData());
        if (response.isSuccess()) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
        }
        return response;
    }

    /**
     * 用户修改头像
     *
     * @param id
     * @param avatarUrl
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult updateAvatar(Long id, String avatarUrl) {
        CompletableFuture<ResponseResult> response = userServiceInterface.updateAvatar(id, avatarUrl).whenCompleteAsync(
                (result, throwable) -> {
                    if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                        throw new RuntimeException(result.getMessage());
                    }
                }, threadPoolExecutor);
        ResponseResult responseResult = null;
        try {
            responseResult = response.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseResult;
    }

    /**
     * 用户修改信息
     *
     * @param userInfoDTO
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult updateUserInfo(UserInfoDTO userInfoDTO) {
        CompletableFuture<ResponseResult> response = userServiceInterface.updateUserInfo(userInfoDTO).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        ResponseResult responseResult = null;
        try {
            responseResult = response.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseResult;
    }

    /**
     * 用户修改密码
     *
     * @param updatePasswordDTO
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        CompletableFuture<ResponseResult> response = userServiceInterface.updatePassword(updatePasswordDTO).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        ResponseResult responseResult = null;
        try {
            responseResult = response.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseResult;
    }
}

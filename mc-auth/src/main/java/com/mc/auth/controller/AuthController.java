package com.mc.auth.controller;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.mc.auth.entity.RegisterUser;
import com.mc.auth.service.AuthService;
import com.mc.common.entity.dto.user.UpdatePasswordDTO;
import com.mc.common.entity.dto.user.UserInfoDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import com.mc.common.utils.RegexUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class AuthController {

    @Resource
    private AuthService authService;


    /**
     * 用户注册
     *
     * @param registerUser
     * @return {@link ResponseResult}
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult register(@Valid @RequestBody RegisterUser registerUser) {
        if (ObjectUtils.isEmpty(registerUser)) {
            return ResponseResult.error(Http.INFO_ERROR.getMessage());
        }
        return authService.register(registerUser);
    }

    /**
     * 用户注销
     *
     * @param token
     * @return {@link ResponseResult}
     */
    @DeleteMapping("/revoke")
    public ResponseResult revoke(@RequestParam("token") String token) {
        if (StringUtils.isBlank(token)) {
            return ResponseResult.error(Http.INFO_ERROR.getMessage());
        }
        return authService.revoke(token);
    }

    /**
     * 校验用户名是否已经注册
     *
     * @param username
     * @return {@link ResponseResult}
     */
    @GetMapping("/oauth/check-username")
    public ResponseResult checkUsername(@RequestParam(required = false) Long id, @RequestParam String username) {
        if (StringUtils.isBlank(username) || !RegexUtils.isUsername(username)) {
            return ResponseResult.error(Http.USERNAME_REFUSE.getMessage());
        }
        return authService.checkUsername(id, username);
    }

    /**
     * 校验手机号是否已经注册
     *
     * @param phone
     * @return {@link ResponseResult}
     */
    @GetMapping("/oauth/check-phone")
    public ResponseResult checkPhone(@RequestParam String phone) {
        if (StringUtils.isBlank(phone) || !RegexUtils.isMobile(phone)) {
            return ResponseResult.error(Http.PHONE_REFUSE.getMessage());
        }
        return authService.checkPhone(phone);
    }

    /**
     * 校验手机号验证码是否正确
     *
     * @param phone
     * @param captcha
     * @return {@link ResponseResult}
     */
    @GetMapping("/oauth/check-captcha")
    public ResponseResult checkCaptcha(@RequestParam String phone, @RequestParam String captcha) {
        if (StringUtils.isBlank(phone) || !RegexUtils.isMobile(phone)) {
            return ResponseResult.error(Http.PHONE_REFUSE.getMessage());
        }
        if (StringUtils.isBlank(captcha) || !RegexUtils.isCaptcha(captcha)) {
            return ResponseResult.error(Http.CAPTCHA_REFUSE.getMessage());
        }
        return authService.checkCaptcha(phone, captcha);
    }

    /**
     * 创建图片验证码
     *
     * @param request
     * @param type
     * @return {@link CaptchaResponse}<{@link ImageCaptchaVO}>
     */
    @RequestMapping("/oauth/captcha/create")
    @ResponseBody
    public CaptchaResponse<ImageCaptchaVO> createCaptcha(HttpServletRequest request,
                                                         @RequestParam(value = "type", required = false) String type) {
        // 如果没有传递type参数，那么默认生成滑块验证码
        if (StringUtils.isBlank(type)) {
            type = CaptchaTypeConstant.SLIDER;
        }
        if ("RANDOM".equals(type)) { // 如果传递的type是RANDOM，那么随机生成一个验证码类型
            // 随机生成一个验证码类型
            int i = ThreadLocalRandom.current().nextInt(0, 2);
            if (i == 0) {
                type = CaptchaTypeConstant.SLIDER; // 滑块验证码
            } else {
                type = CaptchaTypeConstant.ROTATE; // 旋转验证码
            }
        }
        return authService.createCaptcha(type);
    }

    /**
     * 校验图片验证码
     *
     * @param data
     * @param request
     * @return {@link ApiResponse}<{@link ?}>
     */
    @PostMapping("/oauth/captcha/check")
    @ResponseBody
    public ApiResponse<?> checkCaptcha(@RequestBody Data data,
                                       HttpServletRequest request) {
        return authService.checkCaptcha(data);
    }

    @lombok.Data
    public static class Data {
        private String id;
        private ImageCaptchaTrack data;
    }

    /**
     * 用户修改头像
     *
     * @param id
     * @param avatarUrl
     * @return {@link ResponseResult}
     */
    @PutMapping("/oauth/update-avatar")
    public ResponseResult updateAvatar(@RequestParam("id") Long id, @RequestParam("avatarUrl") String avatarUrl) {
        if (ObjectUtils.isEmpty(id)
                || StringUtils.isBlank(avatarUrl)
                || !RegexUtils.isUrl(avatarUrl)) {
            return ResponseResult.error(Http.USER_AVATAR_INFO_ERROR.getMessage());
        }
        return authService.updateAvatar(id, avatarUrl);
    }

    /**
     * 用户信息修改
     *
     * @param userInfoDTO
     * @return {@link ResponseResult}
     */
    @PutMapping("/oauth/update-user")
    public ResponseResult updateUserInfo(@Valid @RequestBody UserInfoDTO userInfoDTO) {
        return authService.updateUserInfo(userInfoDTO);
    }

    /**
     * 用户修改密码
     *
     * @param updatePasswordDTO
     * @return {@link ResponseResult}
     */
    @PutMapping("/oauth/update-password")
    public ResponseResult updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getReNewPassword())) {
            return ResponseResult.error(Http.PASSWORD_NOT_SAME.getMessage());
        }
        return authService.updatePassword(updatePasswordDTO);
    }

}

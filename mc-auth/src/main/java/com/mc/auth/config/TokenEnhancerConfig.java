package com.mc.auth.config;

import com.mc.auth.entity.LoginUser;
import com.mc.common.entity.vo.user.UserSimpleInfoVO;
import com.mc.common.utils.BeanCopyUtils;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu huaiang
 * @description Token增强器
 * @date 2024/02/15
 */
@Component
public class TokenEnhancerConfig implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Map<String, Object> info = new HashMap<>();
        UserSimpleInfoVO userSimpleInfoVO = BeanCopyUtils.copyBean(loginUser.getUser(), UserSimpleInfoVO.class);
        info.put("userInfo", userSimpleInfoVO);
        info.put("roles", loginUser.getRoleId());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
        return accessToken;
    }
}

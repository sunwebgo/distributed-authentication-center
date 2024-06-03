package com.mc.gateway.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mc.common.entity.vo.user.UserSimpleInfoVO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Xu huaiang
 * @date 2024/02/15
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TokenCheckInfo {
    private static final long serialVersionUID = 1L;

    private UserSimpleInfoVO userInfo;

    @JsonProperty("user_name")
    private String username;

    private List<Integer> roles;

    @JsonProperty("client_id")
    private String clientId;

    private String active;

    private String exp;

    private List<String> scope;

}

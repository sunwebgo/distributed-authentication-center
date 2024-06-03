package com.mc.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * token是否刷新的凭证
 * @author Xu huaiang
 * @date 2024/03/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenEvidence {
    private Boolean isRefresh;
    private String token;
}

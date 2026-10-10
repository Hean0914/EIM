package com.company.enterprise.vo;

import com.company.enterprise.common.TokenUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private TokenUser user;
}

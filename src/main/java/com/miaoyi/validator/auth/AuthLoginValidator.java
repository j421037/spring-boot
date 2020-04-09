package com.miaoyi.validator.auth;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AuthLoginValidator {

    @NotNull(message = "账号不能为空")
    @Size(min = 5, max = 15, message = "账号长度为 5 - 15位")
    private String username;

    @NotNull(message = "请输入密码")
    @Pattern(regexp = "[a-zA-Z0-9\\.\\_\\+]{5,15}$", message = "密码只能是数字、字母、符号并且5 - 15位")
    private String password;
}

package com.kfu.timetracking.requests.auth;

import com.kfu.timetracking.requests.auth.enums.RoleEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
    
    @NotNull
    private RoleEnum role;
    
    private Long studentId;
}

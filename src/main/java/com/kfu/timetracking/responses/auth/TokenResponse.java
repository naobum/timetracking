package com.kfu.timetracking.responses.auth;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Cookie accessCookie;
    private Cookie refreshCookie;
}

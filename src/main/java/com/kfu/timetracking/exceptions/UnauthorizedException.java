package com.kfu.timetracking.exceptions;

/**
 * Исключение, выбрасываемое когда пользователь не аутентифицирован.
 * Соответствует HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

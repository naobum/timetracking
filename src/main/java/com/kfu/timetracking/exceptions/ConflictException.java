package com.kfu.timetracking.exceptions;

/**
 * Исключение, выбрасываемое когда возникает конфликт при обработке данных.
 * Соответствует HTTP 400 Bad Request.
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

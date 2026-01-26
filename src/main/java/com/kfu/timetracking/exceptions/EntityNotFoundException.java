package com.kfu.timetracking.exceptions;

/**
 * Исключение, выбрасываемое когда запрошенный ресурс не найден.
 * Соответствует HTTP 404 Not Found.
 */
public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

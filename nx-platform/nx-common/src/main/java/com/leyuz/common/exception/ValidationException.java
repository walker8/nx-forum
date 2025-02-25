package com.leyuz.common.exception;

import java.io.Serial;

public class ValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4580161743507330390L;

    public ValidationException(String message) {
        super(message);
    }
}

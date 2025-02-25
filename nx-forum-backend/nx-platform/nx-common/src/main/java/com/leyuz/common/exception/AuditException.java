package com.leyuz.common.exception;

import java.io.Serial;

public class AuditException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4580161743507330390L;

    public AuditException(String message) {
        super(message);
    }
}

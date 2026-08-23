package com.leyuz.bbs.system.audit.ai;

/**
 * AI 审核异常
 *
 * @author Walker
 */
public class AiModerationException extends RuntimeException {
    public AiModerationException(String message) {
        super(message);
    }

    public AiModerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

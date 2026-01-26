package vn.io.oldmoon.shopizer.user.bussiness.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;
    private final List<String> errors;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.errors = null;
    }
}

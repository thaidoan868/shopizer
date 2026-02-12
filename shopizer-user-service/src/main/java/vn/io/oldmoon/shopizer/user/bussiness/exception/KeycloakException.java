package vn.io.oldmoon.shopizer.user.bussiness.exception;

public class KeycloakException extends RuntimeException {
    private final String path;
    private final String message;
    private final Integer status;

    public KeycloakException(String path, String message, Integer status) {
        super(message);
        this.path = path;
        this.message = message;
        this.status = status;
    }
}

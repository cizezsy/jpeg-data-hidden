package me.cizezsy.exception;

public class BitIOException extends Exception {

    public BitIOException() {
    }

    public BitIOException(String message) {
        super(message);
    }

    public BitIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitIOException(Throwable cause) {
        super(cause);
    }

    public BitIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

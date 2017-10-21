package me.cizezsy.exception;

public class JPEGDecoderException extends Exception{

    public JPEGDecoderException() {
    }

    public JPEGDecoderException(String message) {
        super(message);
    }

    public JPEGDecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public JPEGDecoderException(Throwable cause) {
        super(cause);
    }

    public JPEGDecoderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

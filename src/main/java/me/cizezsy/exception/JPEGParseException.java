package me.cizezsy.exception;

public class JPEGParseException extends Exception {
    public JPEGParseException() {
    }

    public JPEGParseException(String s) {
        super(s);
    }

    public JPEGParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JPEGParseException(Throwable throwable) {
        super(throwable);
    }

    public JPEGParseException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}

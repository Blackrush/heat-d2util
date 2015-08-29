package org.heat.dofus.d2o;

public class MalformedD2oFileException extends RuntimeException {
    public MalformedD2oFileException() {
    }

    public MalformedD2oFileException(String message) {
        super(message);
    }

    public MalformedD2oFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedD2oFileException(Throwable cause) {
        super(cause);
    }

    public MalformedD2oFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

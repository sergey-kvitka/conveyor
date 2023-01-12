package com.kvitka.conveyor.exceptions;

public class PreScoreException extends IllegalArgumentException {
    public PreScoreException() {
    }

    public PreScoreException(String s) {
        super(s);
    }

    public PreScoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreScoreException(Throwable cause) {
        super(cause);
    }
}

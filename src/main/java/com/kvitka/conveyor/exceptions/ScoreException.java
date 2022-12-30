package com.kvitka.conveyor.exceptions;

public class ScoreException extends IllegalArgumentException {
    public ScoreException() {
    }

    public ScoreException(String s) {
        super(s);
    }

    public ScoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScoreException(Throwable cause) {
        super(cause);
    }
}

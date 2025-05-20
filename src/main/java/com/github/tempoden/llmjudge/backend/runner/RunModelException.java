package com.github.tempoden.llmjudge.backend.runner;

public class RunModelException extends RuntimeException {
    public RunModelException(String message) {
        super(message);
    }

    public RunModelException(String message, Throwable cause) {
        super(message, cause);
    }
}

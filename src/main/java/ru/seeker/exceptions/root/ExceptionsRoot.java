package ru.seeker.exceptions.root;

import lombok.Getter;

@Getter
public class ExceptionsRoot extends RuntimeException {
    private final String errorCode;

    public ExceptionsRoot(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

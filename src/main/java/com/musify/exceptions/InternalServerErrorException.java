package com.musify.exceptions;

public class InternalServerErrorException
    extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }
}

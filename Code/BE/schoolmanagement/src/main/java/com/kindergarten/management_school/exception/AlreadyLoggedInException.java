package com.kindergarten.management_school.exception;

public class AlreadyLoggedInException extends RuntimeException{
    public AlreadyLoggedInException(String message) {
        super(message);
    }
}

package com.example.attachfile.exception;

public class MaxPendingApplicationsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MaxPendingApplicationsException(String message) {
        super(message);
    }
}

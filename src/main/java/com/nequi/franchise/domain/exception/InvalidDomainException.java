package com.nequi.franchise.domain.exception;

public class InvalidDomainException extends RuntimeException {
    public InvalidDomainException(String message) {
        super(message);
    }
}

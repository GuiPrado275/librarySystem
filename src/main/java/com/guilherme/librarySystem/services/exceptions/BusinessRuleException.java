package com.guilherme.librarySystem.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//Thrown when a domain/business rule is violated (loan limit reached, no copies available,
//duplicate reservation, overdue user trying to borrow again, etc.)
@ResponseStatus(value = HttpStatus.CONFLICT)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
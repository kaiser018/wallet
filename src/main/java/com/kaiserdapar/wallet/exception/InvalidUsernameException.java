package com.kaiserdapar.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidUsernameException extends RuntimeException {

    public InvalidUsernameException() {
        super("Username cannot be null or blank");
    }

}

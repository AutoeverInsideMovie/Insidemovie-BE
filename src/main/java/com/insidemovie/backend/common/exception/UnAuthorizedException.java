package com.insidemovie.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends BaseException {

    public UnAuthorizedException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }

    public UnAuthorizedException() {
        super(HttpStatus.UNAUTHORIZED);
    }

    public UnAuthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

package com.insidemovie.backend.common.exception;

import org.springframework.http.HttpStatus;

public class IllegalStateException extends BaseException {
    public IllegalStateException() { super(HttpStatus.BAD_REQUEST); }

  public IllegalStateException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

}


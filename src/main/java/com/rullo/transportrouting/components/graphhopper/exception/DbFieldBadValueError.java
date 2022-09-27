package com.rullo.transportrouting.components.graphhopper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for bad value in database field.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DbFieldBadValueError extends RuntimeException {

  public DbFieldBadValueError() {
  }

  public DbFieldBadValueError(String message) {
    super(message);
  }

  public DbFieldBadValueError(String message, Throwable cause) {
    super(message, cause);
  }

  public DbFieldBadValueError(Throwable cause) {
    super(cause);
  }

  public DbFieldBadValueError(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

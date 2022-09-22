package com.rullo.mulhousetransportrouting.components.graphhopper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for delete graphhopper directory error.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CreateGraphhopperDirectoryError extends RuntimeException {

  public CreateGraphhopperDirectoryError() {
  }

  public CreateGraphhopperDirectoryError(String message) {
    super(message);
  }

  public CreateGraphhopperDirectoryError(String message, Throwable cause) {
    super(message, cause);
  }

  public CreateGraphhopperDirectoryError(Throwable cause) {
    super(cause);
  }

  public CreateGraphhopperDirectoryError(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

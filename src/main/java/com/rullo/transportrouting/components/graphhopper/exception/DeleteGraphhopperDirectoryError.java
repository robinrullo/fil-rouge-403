package com.rullo.transportrouting.components.graphhopper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for delete graphhopper directory error.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DeleteGraphhopperDirectoryError extends RuntimeException {

  public DeleteGraphhopperDirectoryError() {
  }

  public DeleteGraphhopperDirectoryError(String message) {
    super(message);
  }

  public DeleteGraphhopperDirectoryError(String message, Throwable cause) {
    super(message, cause);
  }

  public DeleteGraphhopperDirectoryError(Throwable cause) {
    super(cause);
  }

  public DeleteGraphhopperDirectoryError(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

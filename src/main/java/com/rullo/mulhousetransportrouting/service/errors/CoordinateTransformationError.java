package com.rullo.mulhousetransportrouting.service.errors;

/**
 * Error for coordinate transformation.
 */
public class CoordinateTransformationError extends RuntimeException {

  public CoordinateTransformationError() {
  }

  public CoordinateTransformationError(String message) {
    super(message);
  }

  public CoordinateTransformationError(String message, Throwable cause) {
    super(message, cause);
  }

  public CoordinateTransformationError(Throwable cause) {
    super(cause);
  }

  public CoordinateTransformationError(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

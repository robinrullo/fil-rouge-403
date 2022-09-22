package com.rullo.mulhousetransportrouting.service.errors;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error for GraphHopper routing failure.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class GraphhopperRoutingFailureError extends RuntimeException {

  public GraphhopperRoutingFailureError() {
  }

  public GraphhopperRoutingFailureError(String message) {
    super(message);
  }

  public GraphhopperRoutingFailureError(String message, Throwable cause) {
    super(message, cause);
  }

  public GraphhopperRoutingFailureError(Throwable cause) {
    super(cause);
  }

  public GraphhopperRoutingFailureError(List<Throwable> causes) {
    super(causes.stream().map(Throwable::getMessage).collect(Collectors.joining(",")));
  }

  public GraphhopperRoutingFailureError(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

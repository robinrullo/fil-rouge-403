package com.rullo.transportrouting.components.graphhopper;

import static org.junit.jupiter.api.Assertions.*;
import static com.rullo.transportrouting.components.graphhopper.Utils.asUnchecked;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class UtilsTest {

  Executable asUncheckedThrowsException() {
    return () -> {
      throw asUnchecked(new Exception("test"));
    };
  }

  @Test
  void should_throw_runtime_exception() {
    assertThrows(RuntimeException.class, asUncheckedThrowsException());
  }
}

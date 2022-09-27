package com.rullo.transportrouting.entity.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Request Body for routing.
 *
 * @param profile profile to use for routing.
 * @param points  points to route between.
 */
public record RouteRequest(String profile, double[][] points) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteRequest routeRequest = (RouteRequest) o;
    return Objects.equals(profile, routeRequest.profile) && Arrays.deepEquals(points,
        routeRequest.points);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(profile);
    result = 31 * result + Arrays.deepHashCode(points);
    return result;
  }

  @Override
  public String toString() {
    return "routeRequest{" + "points=" + Arrays.toString(points) + ", profile=" + profile + '}';
  }
}

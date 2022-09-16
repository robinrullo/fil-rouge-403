package com.rullo.mulhousetransportrouting.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Itinerary Entity.
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "itineraries")
public class Itinerary extends Feature {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "pois")
  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "waypoints_itineraries",
      joinColumns = @JoinColumn(name = "itinerary_id"),
      inverseJoinColumns = @JoinColumn(name = "poi_id")
  )
  private Set<Poi> pois = new HashSet<>();

}

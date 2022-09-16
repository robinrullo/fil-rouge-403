package com.rullo.mulhousetransportrouting.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POI Entity.
 */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pois")
public class Poi extends Feature {

  @Column(name = "name", nullable = false)
  private String name;
  @Column(name = "address", nullable = true)
  private String address;
}

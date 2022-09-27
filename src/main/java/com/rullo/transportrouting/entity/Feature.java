package com.rullo.transportrouting.entity;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tec.uom.lib.common.function.Identifiable;

/**
 * Feature Entity.
 *
 * @param <G> Geometry Type
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Feature<G extends Geometry> implements Identifiable<Long> {

  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  public Long id;


  @Column(name = "geometry")
  public G geometry;
}

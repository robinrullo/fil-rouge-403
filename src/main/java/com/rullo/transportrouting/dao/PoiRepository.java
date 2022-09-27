package com.rullo.transportrouting.dao;

import com.rullo.transportrouting.entity.Poi;
import com.rullo.transportrouting.entity.dto.PoiProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * POI Repository.
 */

@RepositoryRestResource(excerptProjection = PoiProjection.class)
public interface PoiRepository extends CrudRepository<Poi, Long> {

}

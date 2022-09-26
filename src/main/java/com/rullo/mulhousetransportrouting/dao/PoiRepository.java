package com.rullo.mulhousetransportrouting.dao;

import com.rullo.mulhousetransportrouting.entity.Poi;
import com.rullo.mulhousetransportrouting.entity.dto.PoiProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * POI Repository.
 */

@RepositoryRestResource(excerptProjection = PoiProjection.class)
public interface PoiRepository extends CrudRepository<Poi, Long> {

}

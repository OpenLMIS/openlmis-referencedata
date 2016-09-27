package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface GeographicZoneRepository extends PagingAndSortingRepository<GeographicZone, UUID> {

  @Override
  <S extends GeographicZone> S save(S entity);

  @Override
  <S extends GeographicZone> Iterable<S> save(Iterable<S> entities);
}

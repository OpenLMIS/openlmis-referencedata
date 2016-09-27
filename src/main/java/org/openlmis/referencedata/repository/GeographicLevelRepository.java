package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.GeographicLevel;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface GeographicLevelRepository extends
    PagingAndSortingRepository<GeographicLevel, UUID> {

  @Override
  <S extends GeographicLevel> S save(S entity);

  @Override
  <S extends GeographicLevel> Iterable<S> save(Iterable<S> entities);
}

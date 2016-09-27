package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.FacilityType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface FacilityTypeRepository extends PagingAndSortingRepository<FacilityType, UUID> {

  @Override
  <S extends FacilityType> S save(S entity);

  @Override
  <S extends FacilityType> Iterable<S> save(Iterable<S> entities);
}

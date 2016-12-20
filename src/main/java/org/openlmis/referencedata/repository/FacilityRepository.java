package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.javers.spring.annotation.JaversSpringDataAuditable;

import java.util.UUID;

@JaversSpringDataAuditable
public interface FacilityRepository
    extends PagingAndSortingRepository<Facility, UUID>, FacilityRepositoryCustom {

  @Override
  <S extends Facility> S save(S entity);

  @Override
  <S extends Facility> Iterable<S> save(Iterable<S> entities);
  
  Facility findFirstByCode(String code);
}

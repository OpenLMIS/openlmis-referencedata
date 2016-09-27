package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.FacilityOperator;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface FacilityOperatorRepository
    extends PagingAndSortingRepository<FacilityOperator, UUID> {
    //Add custom FacilityOperator related members here. See UserRepository.java for examples.
}

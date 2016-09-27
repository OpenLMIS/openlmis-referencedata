package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.repository.custom.ProcessingPeriodRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProcessingPeriodRepository extends
    PagingAndSortingRepository<ProcessingPeriod, UUID>,
    ProcessingPeriodRepositoryCustom {

  ProcessingPeriod findFirst1ByOrderByEndDateDesc();
}

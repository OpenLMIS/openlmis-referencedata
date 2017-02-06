package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.ProgramOrderableRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProgramOrderableRepository extends
        PagingAndSortingRepository<ProgramOrderable, UUID>,
    ProgramOrderableRepositoryCustom {

}

package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Right;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface RightRepository extends PagingAndSortingRepository<Right, UUID> {

  Right findFirstByName(String name);
}

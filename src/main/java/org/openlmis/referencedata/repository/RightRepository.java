package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Right;

import java.util.UUID;

public interface RightRepository extends ReferenceDataRepository<Right, UUID> {

  Right findFirstByName(String name);
}

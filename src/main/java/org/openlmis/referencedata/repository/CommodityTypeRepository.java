package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.CommodityType;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CommodityTypeRepository extends CrudRepository<CommodityType, UUID> {
}

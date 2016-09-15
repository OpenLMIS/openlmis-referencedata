package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.GlobalProduct;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GlobalProductRepository extends CrudRepository<GlobalProduct, UUID> {
}

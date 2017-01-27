package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderableDisplayCategoryRepository extends
        PagingAndSortingRepository<OrderableDisplayCategory, UUID> {

  OrderableDisplayCategory findByCode(Code code);
}

package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.ProductCategory;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends
        PagingAndSortingRepository<ProductCategory, UUID> {

  ProductCategory findByCode(Code code);
}

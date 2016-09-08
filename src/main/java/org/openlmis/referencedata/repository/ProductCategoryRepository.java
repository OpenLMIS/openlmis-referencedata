package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.repository.custom.ProductCategoryRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends
        PagingAndSortingRepository<ProductCategory, UUID>,
    ProductCategoryRepositoryCustom {

}

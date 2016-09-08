package org.openlmis.referencedata.repository.custom;


import org.openlmis.referencedata.domain.ProductCategory;

import java.util.List;

public interface ProductCategoryRepositoryCustom {

  List<ProductCategory> searchProductCategories(String code);
}

package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  /**
   * Finds ProductCategories matching all of provided parameters.
   * @param code code of productCategory.
   * @return list of all ProductCategories matching all of provided parameters.
   */
  public List<ProductCategory> searchProductCategories(String code ) {
    return productCategoryRepository.searchProductCategories(code);
  }
}

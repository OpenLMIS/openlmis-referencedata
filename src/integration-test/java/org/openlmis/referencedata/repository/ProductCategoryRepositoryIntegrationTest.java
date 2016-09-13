package org.openlmis.referencedata.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductCategoryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ProductCategory> {

  @Autowired
  ProductCategoryRepository repository;

  private List<ProductCategory> productCategories;

  CrudRepository<ProductCategory, UUID> getRepository() {
    return this.repository;
  }

  ProductCategory generateInstance() {
    Integer instanceNumber = this.getNextInstanceNumber();
    OrderedDisplayValue displayValue = new OrderedDisplayValue(
        "productCategoryName" + instanceNumber,
        instanceNumber);
    ProductCategory productCategory = ProductCategory.createNew(
        Code.code("productCategoryCode" + instanceNumber),
        displayValue);
    return productCategory;
  }

  @Before
  public void setUp() {
    productCategories = new ArrayList<>();
    for (int usersCount = 0; usersCount < 5; usersCount++) {
      productCategories.add(repository.save(generateInstance()));
    }
  }

  @Test
  public void findByCodeShouldFindOne() {
    ProductCategory search = productCategories.get(0);
    ProductCategory found = repository.findByCode(search.getCode());

    Assert.assertEquals(search, found);
  }

  @Test
  public void findByCodeShouldReturnNull() {
    ProductCategory found = repository.findByCode(null);

    Assert.assertNull(found);
  }
}

package org.openlmis.referencedata.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderableDisplayCategoryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<OrderableDisplayCategory> {

  @Autowired
  OrderableDisplayCategoryRepository repository;

  private List<OrderableDisplayCategory> orderableDisplayCategories;

  CrudRepository<OrderableDisplayCategory, UUID> getRepository() {
    return this.repository;
  }

  OrderableDisplayCategory generateInstance() {
    Integer instanceNumber = this.getNextInstanceNumber();
    OrderedDisplayValue displayValue = new OrderedDisplayValue(
        "orderableDisplayCategoryName" + instanceNumber,
        instanceNumber);
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode" + instanceNumber),
        displayValue);
    return orderableDisplayCategory;
  }

  @Before
  public void setUp() {
    orderableDisplayCategories = new ArrayList<>();
    for (int usersCount = 0; usersCount < 5; usersCount++) {
      orderableDisplayCategories.add(repository.save(generateInstance()));
    }
  }

  @Test
  public void findByCodeShouldFindOne() {
    OrderableDisplayCategory search = orderableDisplayCategories.get(0);
    OrderableDisplayCategory found = repository.findByCode(search.getCode());

    Assert.assertEquals(search, found);
  }

  @Test
  public void findByCodeShouldReturnNull() {
    OrderableDisplayCategory found = repository.findByCode(null);

    Assert.assertNull(found);
  }
}

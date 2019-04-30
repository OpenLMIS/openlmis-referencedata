/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class OrderableDisplayCategoryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<OrderableDisplayCategory> {

  @Autowired
  OrderableDisplayCategoryRepository repository;

  private List<OrderableDisplayCategory> orderableDisplayCategories;

  CrudRepository<OrderableDisplayCategory, UUID> getRepository() {
    return this.repository;
  }

  OrderableDisplayCategory generateInstance() {
    OrderableDisplayCategory orderableDisplayCategory =
        new OrderableDisplayCategoryDataBuilder().buildAsNew();
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

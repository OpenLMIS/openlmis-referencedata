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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.referencedata.domain.CommodityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.UUID;

public class CommodityTypeRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<CommodityType> {

  @Autowired
  private CommodityTypeRepository repository;

  @Override
  CrudRepository<CommodityType, UUID> getRepository() {
    return repository;
  }

  @Override
  CommodityType generateInstance() {
    return new CommodityType(null, "Name" + getNextInstanceNumber(),
            "cSys", "cId", null, new ArrayList<>());
  }

  @Test
  public void shouldSaveWithParent() {
    CommodityType commodityType = generateInstance();
    CommodityType child = generateInstance();
    CommodityType grandChild1 = generateInstance();
    CommodityType grandChild2 = generateInstance();

    child.assignParent(commodityType);
    grandChild1.assignParent(child);
    grandChild2.assignParent(child);

    repository.save(asList(commodityType, child, grandChild1, grandChild2));

    commodityType = repository.findOne(commodityType.getId());
    child = repository.findOne(child.getId());
    grandChild1 = repository.findOne(grandChild1.getId());
    grandChild2 = repository.findOne(grandChild2.getId());

    assertNull(commodityType.getParent());
    assertEquals(singletonList(child), commodityType.getChildren());
    assertEquals(commodityType, child.getParent());
    assertEquals(asList(grandChild1, grandChild2), child.getChildren());
    assertEquals(child, grandChild1.getParent());
    assertEquals(child, grandChild2.getParent());
    assertThat(grandChild1.getChildren(), empty());
    assertThat(grandChild2.getChildren(), empty());
  }
}

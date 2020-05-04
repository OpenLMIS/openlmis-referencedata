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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.testbuilder.CommodityTypeDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class CommodityTypeRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<CommodityType> {

  private static final String CLASSIFICATION_ID = "cId";
  private static final String CLASSIFICATION_SYSTEM = "cSys";

  @Autowired
  private CommodityTypeRepository repository;

  @Override
  CrudRepository<CommodityType, UUID> getRepository() {
    return repository;
  }

  @Override
  CommodityType generateInstance() {
    return new CommodityTypeDataBuilder()
        .withClassificationSystem(CLASSIFICATION_SYSTEM)
        .withClassificationId(CLASSIFICATION_ID)
        .buildAsNew();
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

    repository.saveAll(asList(commodityType, child, grandChild1, grandChild2));

    commodityType = repository.findById(commodityType.getId()).orElse(null);
    child = repository.findById(child.getId()).orElse(null);
    grandChild1 = repository.findById(grandChild1.getId()).orElse(null);
    grandChild2 = repository.findById(grandChild2.getId()).orElse(null);

    assertNull(commodityType.getParent());
    assertEquals(singletonList(child), commodityType.getChildren());
    assertEquals(commodityType, child.getParent());
    assertEquals(asList(grandChild1, grandChild2), child.getChildren());
    assertEquals(child, grandChild1.getParent());
    assertEquals(child, grandChild2.getParent());
    assertThat(grandChild1.getChildren(), empty());
    assertThat(grandChild2.getChildren(), empty());
  }

  @Test
  public void shouldFindCommodityTypeByClassificationIdAndSystem() {
    assertFalse(repository.findByClassificationIdAndClassificationSystem(CLASSIFICATION_ID,
        CLASSIFICATION_SYSTEM).isPresent());

    CommodityType commodityType = generateInstance();
    commodityType = repository.save(commodityType);

    assertTrue(repository.findByClassificationIdAndClassificationSystem(CLASSIFICATION_ID,
        CLASSIFICATION_SYSTEM).isPresent());
    assertEquals(commodityType, repository.findByClassificationIdAndClassificationSystem(
        CLASSIFICATION_ID, CLASSIFICATION_SYSTEM).get());
  }
}

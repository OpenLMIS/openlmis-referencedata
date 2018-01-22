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

package org.openlmis.referencedata.domain;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.referencedata.testbuilder.CommodityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;

public class TradeItemTest {
  private static final String CID_1 = "CID1";
  private static final String CID_2 = "ID2";

  private CommodityType ibuprofen = new CommodityTypeDataBuilder().build();
  private TradeItem advil = new TradeItemDataBuilder().build();

  @Test
  public void canFulfillWithClassification() {
    assertFalse(advil.canFulfill(ibuprofen));

    advil = new TradeItemDataBuilder()
        .withClassification(ibuprofen)
        .build();

    assertTrue(advil.canFulfill(ibuprofen));
  }

  @Test
  public void canFulfillWithChildClassification() {
    assertFalse(advil.canFulfill(ibuprofen));

    CommodityType child = new CommodityTypeDataBuilder().build();
    assertFalse(advil.canFulfill(child));

    advil.assignCommodityType(child);
    ibuprofen.setChildren(Lists.newArrayList(child));

    assertTrue(advil.canFulfill(ibuprofen));
    assertTrue(advil.canFulfill(child));
  }

  @Test
  public void shouldAssignClassifications() {
    advil.assignCommodityType("csys", CID_1);
    advil.assignCommodityType("test sys", CID_2);

    assertThat(advil.getClassifications(), hasSize(2));

    assertClassification(CID_1, "csys");
    assertClassification(CID_2, "test sys");

    advil.assignCommodityType("csys changed", CID_1);

    assertThat(advil.getClassifications(), hasSize(2));

    assertClassification(CID_1, "csys changed");
    assertClassification(CID_2, "test sys");
  }

  private void assertClassification(String id, String system) {
    TradeItemClassification classification = advil.findClassificationById(id);
    assertNotNull(classification);
    assertThat(classification.getClassificationSystem(), is(system));
  }
}

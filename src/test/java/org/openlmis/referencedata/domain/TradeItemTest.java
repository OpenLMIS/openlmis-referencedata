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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TradeItemTest {
  //  private static TradeItem advil;
  //
  //  private static final String ADVIL_CODE = "advil";
  //  private static final String ADVIL_NAME = "Advil";
  //  private static final String CID1 = "CID1";
  //
  //  {
  //    advil = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 10, 5, false);
  //  }
  //
  //  @Test
  //  public void testPacksToOrder() throws Exception {
  //    assertEquals(0, advil.packsToOrder(-1));
  //    assertEquals(0, advil.packsToOrder(0));
  //    assertEquals(1, advil.packsToOrder(1));
  //    assertEquals(1, advil.packsToOrder(10));
  //    assertEquals(1, advil.packsToOrder(11));
  //    assertEquals(1, advil.packsToOrder(15));
  //    assertEquals(2, advil.packsToOrder(16));
  //    assertEquals(2, advil.packsToOrder(17));
  //  }
  //
  //  @Test
  //  public void testCanFulfillWhenHasCommodityType() throws Exception {
  //    assertTrue(advil.canFulfill(advil));
  //  }
  //
  //  @Test
  //  public void testEqualsAndHashCode() {
  //    assertTrue(advil.equals(advil));
  //
  //    TradeItem advilDupe = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 20, 10, false);
  //    assertTrue(advil.equals(advilDupe));
  //    assertEquals(advil.hashCode(), advilDupe.hashCode());
  //  }
  //
  //  @Test
  //  public void shouldAssignClassifications() {
  //    advil.assignCommodityType("csys", CID1);
  //    advil.assignCommodityType("test sys", "ID2");
  //
  //    assertThat(advil.getClassifications(), hasSize(2));
  //    TradeItemClassification classification = advil.findClassificationById(CID1);
  //    assertThat(classification.getClassificationSystem(), is("csys"));
  //
  //    advil.assignCommodityType("csys changed", CID1);
  //
  //    assertThat(advil.getClassifications(), hasSize(2));
  //    classification = advil.findClassificationById(CID1);
  //    assertThat(classification.getClassificationSystem(), is("csys changed"));
  //  }
}

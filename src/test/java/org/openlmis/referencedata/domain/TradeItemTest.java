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

import org.openlmis.referencedata.exception.ValidationMessageException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TradeItemTest {
  private static CommodityType ibuprofen;
  private static TradeItem advil;

  private static final String ADVIL_CODE = "advil";
  private static final String ADVIL_NAME = "Advil";


  {
    ibuprofen = CommodityType.newCommodityType(
        "ibuprofen", "each", "Ibuprofen", "test", 30, 15, false);
    advil = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 10, 5, false);
    ibuprofen.addTradeItem(advil);
  }

  @Test
  public void testPacksToOrder() throws Exception {
    assertEquals(0, advil.packsToOrder(-1));
    assertEquals(0, advil.packsToOrder(0));
    assertEquals(1, advil.packsToOrder(1));
    assertEquals(1, advil.packsToOrder(10));
    assertEquals(1, advil.packsToOrder(11));
    assertEquals(1, advil.packsToOrder(15));
    assertEquals(2, advil.packsToOrder(16));
    assertEquals(2, advil.packsToOrder(17));
  }

  @Test
  public void testCanFulfillWhenHasCommodityType() throws Exception {
    assertTrue(advil.canFulfill(ibuprofen));
    assertTrue(advil.canFulfill(advil));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(advil.equals(advil));

    TradeItem advilDupe = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 20, 10, false);
    ibuprofen.addTradeItem(advilDupe);
    assertTrue(advil.equals(advilDupe));
    assertEquals(advil.hashCode(), advilDupe.hashCode());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenAssigningCommodityTypeWithWrongDispensingUnit() {
    TradeItem motrin = TradeItem.newTradeItem("motrin", "10 tab strip", "Motrin", 20, 10, false);
    ibuprofen.addTradeItem(motrin);
  }

}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CommodityTypeTest {
  private static final String EACH = "each";

  private static CommodityType ibuprofen;
  private static TradeItem advil;
  private static TradeItem motrin;

  {
    ibuprofen =
        CommodityType.newCommodityType("ibuprofen", EACH, "Ibuprofen", "test desc", 10, 5, false);
    advil = TradeItem.newTradeItem("advil", EACH, "Advil", 12, 6, false);
    motrin = TradeItem.newTradeItem("motrin", EACH, "Motrin", 12, 6, false);
    ibuprofen.addTradeItem(advil);
    ibuprofen.addTradeItem(motrin);
  }

  @Test
  public void testCanFulfill() throws Exception {
    assertTrue(ibuprofen.canFulfill(ibuprofen));
    assertFalse(ibuprofen.canFulfill(advil));
    assertFalse(ibuprofen.canFulfill(motrin));
  }

  @Test
  public void testPacksToOrder() {
    assertEquals(0, ibuprofen.packsToOrder(-1));
    assertEquals(0, ibuprofen.packsToOrder(0));
    assertEquals(1, ibuprofen.packsToOrder(1));
    assertEquals(1, ibuprofen.packsToOrder(10));
    assertEquals(1, ibuprofen.packsToOrder(11));
    assertEquals(1, ibuprofen.packsToOrder(15));
    assertEquals(2, ibuprofen.packsToOrder(16));
    assertEquals(2, ibuprofen.packsToOrder(17));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(ibuprofen.equals(ibuprofen));

    CommodityType ibuprofenDupe =
        CommodityType.newCommodityType("ibuprofen", EACH, "Ibuprofen", "dupe", 20, 10, false);
    assertEquals(ibuprofen.hashCode(), ibuprofenDupe.hashCode());
  }
}
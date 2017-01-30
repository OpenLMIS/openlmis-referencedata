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

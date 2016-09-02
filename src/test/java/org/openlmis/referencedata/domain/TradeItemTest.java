package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TradeItemTest {
  private static GlobalProduct ibuprofen;
  private static TradeItem advil;

  {
    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "test", 30);
    advil = TradeItem.newTradeItem("advil", 10);
    ibuprofen.addTradeItem(advil);
  }

  @Test
  public void testPacksToOrder() throws Exception {
    assertEquals(0, advil.packsToOrder(-1));
    assertEquals(0, advil.packsToOrder(0));
    assertEquals(1, advil.packsToOrder(1));
    assertEquals(1, advil.packsToOrder(10));
    assertEquals(2, advil.packsToOrder(11));
  }

  @Test
  public void testCanFulfill() throws Exception {
    assertTrue(advil.canFulfill(ibuprofen));
    assertTrue(advil.canFulfill(advil));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(advil.equals(advil));

    TradeItem advilDupe = TradeItem.newTradeItem("advil", 20);
    ibuprofen.addTradeItem(advilDupe);
    assertTrue(advil.equals(advilDupe));
    assertEquals(advil.hashCode(), advilDupe.hashCode());
  }
}
package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GlobalProductTest {
  private static GlobalProduct ibuprofen;
  private static TradeItem advil;
  private static TradeItem motrin;

  {
    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "Ibuprofen", "test desc", 10);
    advil = TradeItem.newTradeItem("advil", "Advil", 12);
    motrin = TradeItem.newTradeItem("motrin", "Motrin", 12);
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
    assertEquals(2, ibuprofen.packsToOrder(11));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(ibuprofen.equals(ibuprofen));

    GlobalProduct ibuprofenDupe =
        GlobalProduct.newGlobalProduct("ibuprofen", "Ibuprofen", "dupe", 20);
    assertEquals(ibuprofen.hashCode(), ibuprofenDupe.hashCode());
  }
}
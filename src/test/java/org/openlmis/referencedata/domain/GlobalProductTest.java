package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GlobalProductTest {
  private static final String EACH = "each";

  private static GlobalProduct ibuprofen;
  private static TradeItem advil;
  private static TradeItem motrin;

  {
    ibuprofen =
        GlobalProduct.newGlobalProduct("ibuprofen", EACH, "Ibuprofen", "test desc", 10, 5, false);
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
    assertEquals(2, ibuprofen.packsToOrder(15));
    assertEquals(2, ibuprofen.packsToOrder(17));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(ibuprofen.equals(ibuprofen));

    GlobalProduct ibuprofenDupe =
        GlobalProduct.newGlobalProduct("ibuprofen", EACH, "Ibuprofen", "dupe", 20, 10, false);
    assertEquals(ibuprofen.hashCode(), ibuprofenDupe.hashCode());
  }
}
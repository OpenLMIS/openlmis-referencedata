package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TradeItemTest {
  private static GlobalProduct ibuprofen;
  private static TradeItem advil;
  private static TradeItem advilWithDifferentDispensingUnit;

  private static final String ADVIL_CODE = "advil";
  private static final String ADVIL_NAME = "Advil";


  {
    ibuprofen = GlobalProduct.newGlobalProduct(
        "ibuprofen", "10 tab stripe", "Ibuprofen", "test", 30);
    advil = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 10);
    advilWithDifferentDispensingUnit = TradeItem.newTradeItem(
        ADVIL_CODE, "10 tab stripe", ADVIL_NAME, 10);
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
  public void testCanFulfillWhenHasGlobalProduct() throws Exception {
    assertTrue(advil.canFulfill(ibuprofen));
    assertTrue(advil.canFulfill(advil));
  }

  @Test
  public void shouldNotFulfillWhenDispensingUnitIsDifferent() throws Exception {
    assertFalse(advil.canFulfill(advilWithDifferentDispensingUnit));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertTrue(advil.equals(advil));

    TradeItem advilDupe = TradeItem.newTradeItem(ADVIL_CODE, "each", ADVIL_NAME, 20);
    ibuprofen.addTradeItem(advilDupe);
    assertTrue(advil.equals(advilDupe));
    assertEquals(advil.hashCode(), advilDupe.hashCode());
  }

  @Test
  public void testEqualsWithDifferentDispensingUnit() {

    TradeItem advilDupe = TradeItem.newTradeItem(
        ADVIL_CODE, "60 tab stripe", ADVIL_NAME, 20);
    ibuprofen.addTradeItem(advilDupe);
    assertFalse(advil.equals(advilDupe));
    assertNotEquals(advil.hashCode(), advilDupe.hashCode());
  }

}
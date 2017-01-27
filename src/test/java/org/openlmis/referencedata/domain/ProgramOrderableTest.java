package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.money.CurrencyUnit;
import org.junit.Test;

public class ProgramOrderableTest {

  private static Orderable ibuprofen;
  private static Program em;
  private static OrderableDisplayCategory testCat;

  {
    em = new Program("EM");
    testCat = OrderableDisplayCategory.createNew(Code.code("test"));
    ibuprofen =
        CommodityType.newCommodityType("ibuprofen", "each", "Ibuprofen", "NSAID", 20, 10, false);
  }

  @Test
  public void shouldBeEqualByProgramAndProduct() {
    ProgramOrderable ibuprofenInEm =
        ProgramOrderable.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);

    Program emDupe = new Program("EM");
    OrderableDisplayCategory testCatDupe = OrderableDisplayCategory.createNew(Code.code("catdupe"));
    ProgramOrderable ibuprofenInEmDupe =
        ProgramOrderable.createNew(emDupe, testCatDupe, ibuprofen, CurrencyUnit.USD);

    assertEquals(ibuprofenInEm, ibuprofenInEmDupe);
    assertEquals(ibuprofenInEmDupe, ibuprofenInEm);
    assertEquals(ibuprofenInEm.hashCode(), ibuprofenInEmDupe.hashCode());
  }

  @Test
  public void isForProgramShouldBeTrue() {
    ProgramOrderable ibuprofenInEm =
        ProgramOrderable.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);
    assertTrue(ibuprofenInEm.isForProgram(em));
  }

  @Test
  public void isForProgramShouldBeFalse() {
    ProgramOrderable ibuprofenInEm =
        ProgramOrderable.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);

    assertFalse(ibuprofenInEm.isForProgram(null));
    assertFalse(ibuprofenInEm.isForProgram(new Program("fail")));
  }
}

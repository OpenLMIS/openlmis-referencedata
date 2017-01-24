package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.money.CurrencyUnit;
import org.junit.Test;

public class ProgramProductTest {

  private static OrderableProduct ibuprofen;
  private static Program em;
  private static ProductCategory testCat;

  {
    em = new Program("EM");
    testCat = ProductCategory.createNew(Code.code("test"));
    ibuprofen =
        GlobalProduct.newGlobalProduct("ibuprofen", "each", "Ibuprofen", "NSAID", 20, 10, false);
  }

  @Test
  public void shouldBeEqualByProgramAndProduct() {
    ProgramProduct ibuprofenInEm =
        ProgramProduct.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);

    Program emDupe = new Program("EM");
    ProductCategory testCatDupe = ProductCategory.createNew(Code.code("catdupe"));
    ProgramProduct ibuprofenInEmDupe =
        ProgramProduct.createNew(emDupe, testCatDupe, ibuprofen, CurrencyUnit.USD);

    assertEquals(ibuprofenInEm, ibuprofenInEmDupe);
    assertEquals(ibuprofenInEmDupe, ibuprofenInEm);
    assertEquals(ibuprofenInEm.hashCode(), ibuprofenInEmDupe.hashCode());
  }

  @Test
  public void isForProgramShouldBeTrue() {
    ProgramProduct ibuprofenInEm =
        ProgramProduct.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);
    assertTrue(ibuprofenInEm.isForProgram(em));
  }

  @Test
  public void isForProgramShouldBeFalse() {
    ProgramProduct ibuprofenInEm =
        ProgramProduct.createNew(em, testCat, ibuprofen, CurrencyUnit.USD);

    assertFalse(ibuprofenInEm.isForProgram(null));
    assertFalse(ibuprofenInEm.isForProgram(new Program("fail")));
  }
}

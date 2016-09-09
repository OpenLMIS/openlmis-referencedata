package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProgramProductTest {

  private static OrderableProduct ibuprofen;
  private static Program em;

  {
    em = new Program("EM");
    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "NSAID", 20);
  }

  @Test
  public void shouldBeEqualByProgramAndProduct() {
    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "testcat", ibuprofen);

    Program emDupe = new Program("EM");
    ProgramProduct ibuprofenInEmDupe = ProgramProduct.createNew(emDupe, "catdupe", ibuprofen);

    assertEquals(ibuprofenInEm, ibuprofenInEmDupe);
    assertEquals(ibuprofenInEmDupe, ibuprofenInEm);
    assertEquals(ibuprofenInEm.hashCode(), ibuprofenInEmDupe.hashCode());
  }

  @Test
  public void isForProgramShouldBeTrue() {
    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "test", ibuprofen);
    assertTrue(ibuprofenInEm.isForProgram(em));
  }

  @Test
  public void isForProgramShouldBeFalse() {
    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "test", ibuprofen);

    assertFalse(ibuprofenInEm.isForProgram(null));
    assertFalse(ibuprofenInEm.isForProgram(new Program("fail")));
  }
}

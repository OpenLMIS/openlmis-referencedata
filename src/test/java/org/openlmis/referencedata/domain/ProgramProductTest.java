package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProgramProductTest {

  private static OrderableProduct ibuprofen;
  private static Program em;

  {
    em = new Program();
    em.setCode(Code.code("EM"));

    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "NSAID", 20);
  }

  @Test
  public void shouldBeEqualByProgramAndProduct() {
    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "testcat", ibuprofen);

    Program emDupe = new Program();
    emDupe.setCode(Code.code("EM"));
    ProgramProduct ibuprofenInEmDupe = ProgramProduct.createNew(emDupe, "catdupe", ibuprofen);

    assertEquals(ibuprofenInEm, ibuprofenInEmDupe);
    assertEquals(ibuprofenInEmDupe, ibuprofenInEm);
    assertEquals(ibuprofenInEm.hashCode(), ibuprofenInEmDupe.hashCode());
  }
}

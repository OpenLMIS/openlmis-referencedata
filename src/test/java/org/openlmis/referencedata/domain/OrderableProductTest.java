package org.openlmis.referencedata.domain;

import org.junit.Test;

public class OrderableProductTest {
  private static Program em;
  private static OrderableProduct ibuprofen;

  {
    em = new Program();
    em.setCode(Code.code("EssMed"));

    ibuprofen = GlobalProduct.newGlobalProduct("ibuprofen", "test", 10);

    ProgramProduct ibuprofenInEm = ProgramProduct.createNew(em, "testcat", ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEm);
  }

  @Test
  public void shouldReplaceProgramProductOnEquals() {
    ProgramProduct ibuprofenInEmForNsaid = ProgramProduct.createNew(em, "nsaid", ibuprofen);
    ibuprofen.addToProgram(ibuprofenInEmForNsaid);
  }
}

package org.openlmis.referencedata.domain;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProgramTest {

  @Test
  public void shouldBeEqualByCode() {
    Program epi = new Program();
    epi.setCode(Code.code("EPI"));

    Program epiDupe = new Program();
    epiDupe.setCode(Code.code("epi"));

    assertTrue(epi.equals(epiDupe));
    assertTrue(epiDupe.equals(epi));
  }
}

package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CodeTest {

  private Code codeA;
  private Code codeB;

  {
    codeA = Code.code("codeA");
    codeB = Code.code("codeB");
  }

  @Test
  public void shouldUseBlankProductCodeIfNull() {
    Code code = Code.code(null);
    assertEquals(Code.code(""), code);
  }

  @Test
  public void shouldBeEqualByCode() {
    Code codeADupe = Code.code("codeA");
    assertTrue(codeA.equals(codeADupe));
    assertTrue(codeADupe.equals(codeA));
  }

  @Test
  public void shouldBeEqualIgnoreCase() {
    Code codeADupe = Code.code("codea");
    assertEquals(codeA, codeADupe);
  }

  @Test
  public void shouldBeEqualIgnoreSpace() {
    Code codeADupe = Code.code("code A");
    assertEquals(codeA, codeADupe);
    assertEquals(codeADupe, codeA);
  }

  @Test
  public void shouldEnforceHashCode() {
    Code codeADupe = Code.code("code a");
    assertEquals(codeA.hashCode(), codeADupe.hashCode());

    assertNotEquals(codeA.hashCode(), codeB.hashCode());
  }

  @Test
  public void shouldNotBeEqual() {
    assertNotEquals(codeA, codeB);
  }
}

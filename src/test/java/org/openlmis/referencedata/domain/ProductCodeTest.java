package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProductCodeTest {

  private ProductCode codeA;
  private ProductCode codeB;

  {
    codeA = ProductCode.newProductCode("codeA");
    codeB = ProductCode.newProductCode("codeB");
  }

  @Test
  public void shouldBeEqualByCode() {
    ProductCode codeADupe = ProductCode.newProductCode("codeA");
    assertTrue(codeA.equals(codeADupe));
    assertTrue(codeADupe.equals(codeA));
  }

  @Test
  public void shouldBeEqualIgnoreCase() {
    ProductCode codeADupe = ProductCode.newProductCode("codea");
    assertEquals(codeA, codeADupe);
  }

  @Test
  public void shouldBeEqualIgnoreSpace() {
    ProductCode codeADupe = ProductCode.newProductCode("code A");
    assertEquals(codeA, codeADupe);
  }

  @Test
  public void shouldEnforceHashCode() {
    ProductCode codeADupe = ProductCode.newProductCode("codeA");
    assertEquals(codeA.hashCode(), codeADupe.hashCode());

    assertNotEquals(codeA.hashCode(), codeB.hashCode());
  }

  @Test
  public void shouldNotBeEqual() {
    assertNotEquals(codeA, codeB);
  }
}

package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class OrderedDisplayValueTest {
  @Test
  public void equalsShouldTestDisplayNameAndOrder() {
    OrderedDisplayValue dispMac = new OrderedDisplayValue("mac", 0);
    OrderedDisplayValue dispMac1 = new OrderedDisplayValue("mac", 1);
    OrderedDisplayValue dispCheese = new OrderedDisplayValue("cheese", 0);

    assertNotEquals(dispMac, dispMac1);
    assertNotEquals(dispMac, dispCheese);
  }

  @Test
  public void equalsAndHashcodeShouldIgnoreCaseAndWhitespace() {
    OrderedDisplayValue value = new OrderedDisplayValue(" value ", 0);
    OrderedDisplayValue valueDupe = new OrderedDisplayValue("VaLue", 0);
    assertEquals(value, valueDupe);
    assertEquals(value.hashCode(), valueDupe.hashCode());
  }
}

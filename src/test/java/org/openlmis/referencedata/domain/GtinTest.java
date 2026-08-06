/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_INVALID_CHECK_DIGIT;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_INVALID_LENGTH;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_GTIN_NUMERIC;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.exception.ValidationMessageException;

@SuppressWarnings("PMD.TooManyMethods")
public class GtinTest {

  private static final String GTIN_8 = "96385074";
  private static final String GTIN_12 = "614141000036";
  private static final String GTIN_13 = "5901234123457";
  private static final String GTIN_14 = "05890123456786";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private Gtin gtin1 = new Gtin("11111111111113");
  private Gtin gtin2 = new Gtin("22222220");

  @Test
  public void shouldBeEqualByGtin() {
    Gtin gtin1Duplicate = new Gtin(gtin1.getGtin());
    assertTrue(gtin1.equals(gtin1Duplicate));
    assertTrue(gtin1Duplicate.equals(gtin1));
  }

  @Test
  public void shouldEnforceHashGtin() {
    Gtin gtin1Duplicate = new Gtin(gtin1.getGtin());
    assertEquals(gtin1.hashCode(), gtin1Duplicate.hashCode());
    assertNotEquals(gtin1.hashCode(), gtin2.hashCode());
  }

  @Test
  public void shouldNotBeEqual() {
    assertNotEquals(gtin1, gtin2);
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Gtin.class)
        .verify();
  }

  @Test
  public void shouldKeepGtin14AsIs() {
    assertEquals(GTIN_14, new Gtin(GTIN_14).getGtin());
  }

  @Test
  public void shouldPadGtin13To14Digits() {
    assertEquals("05901234123457", new Gtin(GTIN_13).getGtin());
  }

  @Test
  public void shouldPadGtin12To14Digits() {
    assertEquals("00614141000036", new Gtin(GTIN_12).getGtin());
  }

  @Test
  public void shouldPadGtin8To14Digits() {
    assertEquals("00000096385074", new Gtin(GTIN_8).getGtin());
  }

  @Test
  public void shouldBeEqualToTheSameGtinInAnotherStructure() {
    assertEquals(new Gtin(GTIN_13), new Gtin("05901234123457"));
    assertEquals(new Gtin(GTIN_8), new Gtin("00000096385074"));
  }

  @Test
  public void shouldThrowExceptionIfGtinIsNotNumeric() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_NUMERIC);

    new Gtin("ab12345678ba");
  }

  @Test
  public void shouldThrowExceptionIfGtinUsesNonAsciiDigits() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_NUMERIC);

    // Arabic-Indic digits for 96385074 - accepted by Character.isDigit, unusable by a scanner
    new Gtin(new String(new char[] {0x0669, 0x0666, 0x0663, 0x0668, 0x0665, 0x0660, 0x0667,
        0x0664}));
  }

  @Test
  public void shouldThrowExceptionIfGtinIsNull() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_NUMERIC);

    new Gtin(null);
  }

  @Test
  public void shouldThrowExceptionIfGtinIsEmpty() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_NUMERIC);

    new Gtin("");
  }

  @Test
  public void shouldThrowExceptionIfGtinIsTooShort() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_LENGTH);

    new Gtin("1234567");
  }

  @Test
  public void shouldThrowExceptionIfGtinIsTooLong() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_LENGTH);

    new Gtin("059012341234570");
  }

  @Test
  public void shouldThrowExceptionIfGtinHasLengthBetweenDefinedStructures() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_LENGTH);

    // 9, 10 and 11 digits are not GTIN structures, but were accepted before
    new Gtin("963850740");
  }

  @Test
  public void shouldThrowExceptionIfGtinHasTenDigits() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_LENGTH);

    new Gtin("9638507400");
  }

  @Test
  public void shouldThrowExceptionIfGtinHasElevenDigits() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_LENGTH);

    new Gtin("96385074000");
  }

  @Test
  public void shouldThrowExceptionIfCheckDigitIsInvalid() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_CHECK_DIGIT);

    new Gtin("05890123456787");
  }

  @Test
  public void shouldThrowExceptionIfCheckDigitOfShorterStructureIsInvalid() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_GTIN_INVALID_CHECK_DIGIT);

    new Gtin("96385075");
  }

  @Test
  public void shouldNormalizeToFourteenDigits() {
    assertEquals("05901234123457", Gtin.normalize(GTIN_13));
    assertEquals("00000096385074", Gtin.normalize(GTIN_8));
  }

  @Test
  public void shouldNotChangeValueThatCannotBeNormalized() {
    assertNull(Gtin.normalize(null));
    assertEquals(GTIN_14, Gtin.normalize(GTIN_14));
    assertEquals("059012341234570", Gtin.normalize("059012341234570"));
  }
}

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
import static org.junit.Assert.assertNull;
import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_INVALID_FORMAT;
import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_TOO_LONG;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.exception.ValidationMessageException;

public class LotTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldAcceptCodeWithinLengthAndCharset() {
    Lot lot = new Lot();
    lot.setLotCode("ABC-123/xyz.01");

    assertEquals("ABC-123/xyz.01", lot.getLotCode());
  }

  @Test
  public void shouldAcceptCodeAtMaxLength() {
    Lot lot = new Lot();
    String code = "12345678901234567890";

    lot.setLotCode(code);

    assertEquals(code, lot.getLotCode());
  }

  @Test
  public void shouldAllowNullCode() {
    Lot lot = new Lot();
    lot.setLotCode(null);

    assertNull(lot.getLotCode());
  }

  @Test
  public void shouldThrowExceptionIfCodeIsLongerThanMax() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_LOT_CODE_TOO_LONG);

    new Lot().setLotCode("123456789012345678901");
  }

  @Test
  public void shouldThrowExceptionIfCodeContainsSpace() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_LOT_CODE_INVALID_FORMAT);

    new Lot().setLotCode("AB CD");
  }

  @Test
  public void shouldThrowExceptionIfCodeContainsDisallowedCharacter() {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_LOT_CODE_INVALID_FORMAT);

    new Lot().setLotCode("ABC#123");
  }
}

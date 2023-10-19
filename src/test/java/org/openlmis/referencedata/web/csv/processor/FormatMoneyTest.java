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

package org.openlmis.referencedata.web.csv.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_FORMATTING_FAILED;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

public class FormatMoneyTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private FormatMoney formatMoney;

  @Before
  public void beforeEach() {
    formatMoney = new FormatMoney();
  }

  @Test
  public void shouldFormatValidMoney() {
    Money money = Money.parse("USD 1.23");

    String result = formatMoney.execute(money, context);

    assertEquals("1.23", result);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotMoneyType() {
    String invalid = "invalid-type";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_UPLOAD_FORMATTING_FAILED);

    formatMoney.execute(invalid, context);
  }

}
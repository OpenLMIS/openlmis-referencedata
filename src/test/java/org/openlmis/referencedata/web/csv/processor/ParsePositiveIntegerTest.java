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

import static org.junit.Assert.assertEquals;
import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_PARSING_FAILED;
import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_POSITIVE_OR_ZERO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

@RunWith(MockitoJUnitRunner.class)
public class ParsePositiveIntegerTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private ParsePositiveInteger parseAmount;

  @Before
  public void beforeEach() {
    parseAmount = new ParsePositiveInteger();
  }

  @Test
  public void shouldParseValidAmount() {
    Integer result = (Integer) parseAmount.execute("1000", context);
    assertEquals(Integer.valueOf(1000), result);
  }

  @Test
  public void shouldThrownExceptionWhenParameterIsNotInteger() {
    String value = "abc";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_UPLOAD_PARSING_FAILED);

    parseAmount.execute(value, context);
  }
  
  @Test
  public void shouldThrownExceptionWhenParameterIsNegativeInteger() {
    String value = "-1000";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(ERROR_UPLOAD_POSITIVE_OR_ZERO);

    parseAmount.execute(value, context);
  }

}

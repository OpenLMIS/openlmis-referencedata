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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

public class ParseProcessingPeriodTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private ParseProcessingPeriod parseProcessingPeriod;

  private static final String EXPECTED_MESSAGE =
      "'%s' could not be parsed to Processing Period. Error occurred in column '%s', in row '%s'";

  @Before
  public void beforeEach() {
    ParseProcessingPeriod.SEPARATOR = "|";
    parseProcessingPeriod = new ParseProcessingPeriod();
  }

  @Test
  public void shouldParseValidProcessingPeriod() {
    ProcessingPeriodDto result = (ProcessingPeriodDto) parseProcessingPeriod
        .execute("schedule|period", context);
    assertEquals("period", result.getName());
    assertEquals("schedule", result.getProcessingSchedule().getCode());
  }

  @Test
  public void shouldThrownExceptionWhenParameterIsNotString() {
    String value = "1";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            value, context.getColumnNumber(), context.getRowNumber()
    ));

    parseProcessingPeriod.execute(value, context);
  }

  @Test
  public void shouldThrownExceptionWhenParameterHasNoSeparator() {
    String value = "something";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            value, context.getColumnNumber(), context.getRowNumber()
        ));

    parseProcessingPeriod.execute(value, context);
  }

  @Test
  public void shouldThrownExceptionWhenParameterHasMoreThanTwoParts() {
    String value = "one|two|three";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            value, context.getColumnNumber(), context.getRowNumber()
        ));

    parseProcessingPeriod.execute(value, context);
  }

}

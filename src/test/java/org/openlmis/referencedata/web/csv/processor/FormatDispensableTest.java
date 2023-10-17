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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.dto.DispensableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

public class FormatDispensableTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private FormatDispensable formatDispensable;

  private static final String EXPECTED_MESSAGE =
      "Cannot get dispensing unit or size code from '%s'. "
          + "Error occurred in column '%s', in row '%s'";

  @Before
  public void beforeEach() {
    formatDispensable = new FormatDispensable();
  }

  @Test
  public void shouldFormatValidDispensableWithSizeCode() {
    DispensableDto dispensable = new DispensableDto(null, "size-code", null, "display-unit");

    String result = (String) formatDispensable.execute(dispensable, context);

    assertEquals(StringUtils.joinWith(":", Dispensable.KEY_SIZE_CODE,
            dispensable.getAttributes().get(Dispensable.KEY_SIZE_CODE)), result);
  }

  @Test
  public void shouldFormatValidDispensableWithDispensingUnit() {
    DispensableDto dispensable = new DispensableDto("dispensing-unit", null, null, "display-unit");

    String result = (String) formatDispensable.execute(dispensable, context);

    assertEquals(StringUtils.joinWith(":", Dispensable.KEY_DISPENSING_UNIT,
            dispensable.getAttributes().get(Dispensable.KEY_DISPENSING_UNIT)), result);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotDispensableDtoType() {
    String invalid = "invalid-type";

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        String.format(EXPECTED_MESSAGE,
            invalid, context.getColumnNumber(), context.getRowNumber()
        ));

    formatDispensable.execute(invalid, context);
  }

  @Test
  public void shouldThrownExceptionWhenSizeCodeAndDispensingUnitAreNull() {
    DispensableDto dto = new DispensableDto(null, null, null, "display-unit");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        String.format(EXPECTED_MESSAGE,
            dto, context.getColumnNumber(), context.getRowNumber()
    ));

    formatDispensable.execute(dto, context);
  }

}
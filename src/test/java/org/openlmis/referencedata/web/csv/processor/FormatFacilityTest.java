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
import org.openlmis.referencedata.dto.BasicFacilityDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.supercsv.util.CsvContext;

public class FormatFacilityTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private final CsvContext context = new CsvContext(1, 1, 1);

  private FormatFacility formatFacility;

  private static final String EXPECTED_MESSAGE =
      "Cannot get code from '%s'. Error occurred in column '%s', in row '%s'";

  @Before
  public void beforeEach() {
    formatFacility = new FormatFacility();
  }

  @Test
  public void shouldFormatValidFacility() {
    BasicFacilityDto facility = new BasicFacilityDto();
    facility.setCode("facility-code");

    String result = (String) formatFacility.execute(facility, context);

    assertEquals("facility-code", result);
  }

  @Test
  public void shouldThrownExceptionWhenCodeIsNull() {
    BasicFacilityDto facility = new BasicFacilityDto();
    facility.setCode(null);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            facility, context.getColumnNumber(), context.getRowNumber()
        ));

    formatFacility.execute(facility, context);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotBasicFacilityDtoType() {
    String invalid = "invalid-type";

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        String.format(EXPECTED_MESSAGE,
            invalid, context.getColumnNumber(), context.getRowNumber()
        ));

    formatFacility.execute(invalid, context);
  }

}

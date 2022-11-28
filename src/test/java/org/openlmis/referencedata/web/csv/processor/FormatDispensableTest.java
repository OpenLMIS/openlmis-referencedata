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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openlmis.referencedata.dto.DispensableDto;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

public class FormatDispensableTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private CsvContext csvContext;

  private FormatDispensable formatDispensable;
  private static final String DISPENSING_UNIT = "dispensingUnit";

  @Before
  public void beforeEach() {
    formatDispensable = new FormatDispensable();
  }

  @Test
  public void shouldFormatValidDispensable() throws Exception {
    DispensableDto dispensableDto = new DispensableDto();
    Map<String, String> dispensableAttributes = new HashMap<>();
    dispensableAttributes.put(DISPENSING_UNIT, "dispensing-unit");
    dispensableDto.setAttributes(dispensableAttributes);

    String result = (String) formatDispensable.execute(dispensableDto, csvContext);

    assertEquals(StringUtils.joinWith(":", DISPENSING_UNIT,
            dispensableDto.getDispensingUnit()), result);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotBasicFacilityDtoType() {
    String invalid = "invalid-type";

    expectedException.expect(SuperCsvCellProcessorException.class);
    expectedException.expectMessage(String.format("Cannot get dispensing unit from '%s'.",
            invalid));

    formatDispensable.execute(invalid, csvContext);
  }

}
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
import org.mockito.Mock;
import org.openlmis.referencedata.domain.ContainerDispensable;
import org.openlmis.referencedata.domain.DefaultDispensable;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.VaccineDispensable;
import org.openlmis.referencedata.dto.DispensableDto;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

public class FormatDispensableTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private CsvContext csvContext;

  private FormatDispensable formatDispensable;

  @Before
  public void beforeEach() {
    formatDispensable = new FormatDispensable();
  }

  @Test
  public void shouldFormatValidDefaultDispensable() throws Exception {
    DefaultDispensable dispensable = (DefaultDispensable) Dispensable.createNew("dispensing-unit");

    String result = (String) formatDispensable.execute(dispensable, csvContext);

    assertEquals(StringUtils.joinWith(":", Dispensable.KEY_DISPENSING_UNIT,
            dispensable.getAttributes().get(Dispensable.KEY_DISPENSING_UNIT)), result);
  }

  @Test
  public void shouldFormatValidContainerDispensable() throws Exception {
    DispensableDto dto = new DispensableDto(null, "size-code", null, "display-unit");
    ContainerDispensable dispensable = (ContainerDispensable) Dispensable.createNew(dto);

    String result = (String) formatDispensable.execute(dispensable, csvContext);

    assertEquals(StringUtils.joinWith(":", Dispensable.KEY_SIZE_CODE,
            dispensable.getAttributes().get(Dispensable.KEY_SIZE_CODE)), result);
  }

  @Test
  public void shouldFormatValidVaccineDispensable() throws Exception {
    DispensableDto dto = new DispensableDto(null, "size-code", "route-of-administration",
            "display-unit");
    VaccineDispensable dispensable = (VaccineDispensable) Dispensable.createNew(dto);

    String result = (String) formatDispensable.execute(dispensable, csvContext);

    assertEquals(StringUtils.joinWith(":", Dispensable.KEY_SIZE_CODE,
            dispensable.getAttributes().get(Dispensable.KEY_SIZE_CODE)), result);
  }

  @Test
  public void shouldThrownExceptionWhenValueIsNotDispensableType() {
    String invalid = "invalid-type";

    expectedException.expect(SuperCsvCellProcessorException.class);
    expectedException.expectMessage(String.format("Cannot get dispensing unit or size "
            + "code from '%s'.", invalid));

    formatDispensable.execute(invalid, csvContext);
  }

}
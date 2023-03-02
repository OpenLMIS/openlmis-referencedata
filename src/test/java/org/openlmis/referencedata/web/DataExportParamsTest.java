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

package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openlmis.referencedata.web.DataExportParams.DATA;
import static org.openlmis.referencedata.web.DataExportParams.FORMAT;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.exception.ValidationMessageException;

public class DataExportParamsTest {

  private static final String VALUE = "test-value";

  private Map<String, String> queryMap;

  @Before
  public void setUp() {
    queryMap = new HashMap<>();
    queryMap.put(FORMAT, "init-format-value");
    queryMap.put(DATA, "init-data-value");
  }

  @Test
  public void getFormatShouldReturnValueForKeyFormat() {
    queryMap.replace(FORMAT, VALUE);
    DataExportParams params = new DataExportParams(queryMap);

    assertEquals(VALUE, params.getFormat());
  }

  @Test
  public void getDataShouldReturnValueForKeyData() {
    queryMap.replace(DATA, VALUE);
    DataExportParams params = new DataExportParams(queryMap);

    assertEquals(VALUE, params.getData());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrownExceptionWhenFormatParameterIsMissing() {
    queryMap.remove(FORMAT);
    DataExportParams params = new DataExportParams(queryMap);

    assertNull(params);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrownExceptionWhenDataParameterIsMissing() {
    queryMap.remove(DATA);
    DataExportParams params = new DataExportParams(queryMap);

    assertNull(params);
  }

}
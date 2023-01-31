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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.openlmis.referencedata.exception.ValidationMessageException;

public class DataExportParamsTest {

  private static final String VALUE = "test";

  @Test
  public void getFormatShouldReturnValueForKeyFormat() {
    Map<String, String> queryParamsMap = new HashMap<String, String>() {{
        put("format", VALUE);
      }
    };
    DataExportParams params = new DataExportParams(queryParamsMap);

    assertEquals(VALUE, params.getFormat());
  }

  @Test
  public void getFormatShouldReturnNullIfMapDoesNotContainKeyFormat() {
    DataExportParams params = new DataExportParams(new HashMap<>());

    assertNull(VALUE, params.getFormat());
  }

  @Test
  public void getDataShouldReturnValueForKeyData() {
    Map<String, String> queryParamsMap = new HashMap<String, String>() {{
        put("data", VALUE);
      }
    };
    DataExportParams params = new DataExportParams(queryParamsMap);

    assertEquals(VALUE, params.getData());
  }

  @Test
  public void getDataShouldReturnNullIfMapDoesNotContainKeyData() {
    DataExportParams params = new DataExportParams(new HashMap<>());

    assertNull(VALUE, params.getData());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrownExceptionWhenProvidedParamIsNotOnSupportedList() {
    Map<String, String> queryParamsMap = new HashMap<String, String>() {{
        put("some-param", VALUE);
      }
    };
    DataExportParams params = new DataExportParams(queryParamsMap);

    assertNull(params);
  }

}
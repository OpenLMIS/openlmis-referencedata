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

package org.openlmis.referencedata.errorhandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import javax.servlet.RequestDispatcher;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class RefDataErrorAttributesTest {

  private final RefDataErrorAttributes errorAttributes = new RefDataErrorAttributes();

  @Test
  public void shouldLeaveTheExceptionMessageOutOfServerErrors() {
    Map<String, Object> attributes = errorAttributes.getErrorAttributes(
        errorRequest(500, new IllegalStateException("could not prepare statement; SQL [...]")),
        false);

    assertFalse(attributes.containsKey("message"));
    assertEquals(500, attributes.get("status"));
  }

  @Test
  public void shouldKeepTheMessageOfClientErrors() {
    Map<String, Object> attributes = errorAttributes.getErrorAttributes(
        errorRequest(400, new IllegalArgumentException("Required parameter is missing")), false);

    assertEquals("Required parameter is missing", attributes.get("message"));
  }

  private WebRequest errorRequest(int status, Exception exception) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, exception);
    return new ServletWebRequest(request);
  }
}

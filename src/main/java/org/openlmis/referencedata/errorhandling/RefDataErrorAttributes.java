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

import java.util.Map;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class RefDataErrorAttributes extends DefaultErrorAttributes {

  private static final String STATUS = "status";
  private static final String MESSAGE = "message";
  private static final int FIRST_SERVER_ERROR = 500;

  @Override
  public Map<String, Object> getErrorAttributes(WebRequest webRequest,
      boolean includeStackTrace) {
    Map<String, Object> attributes = super.getErrorAttributes(webRequest, includeStackTrace);
    Object status = attributes.get(STATUS);
    if (status instanceof Integer && (Integer) status >= FIRST_SERVER_ERROR) {
      attributes.remove(MESSAGE);
    }
    return attributes;
  }
}

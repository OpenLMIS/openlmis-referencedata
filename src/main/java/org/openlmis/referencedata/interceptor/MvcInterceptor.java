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

package org.openlmis.referencedata.interceptor;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openlmis.referencedata.errorhandling.RefDataErrorHandling;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.OrderableDisplayCategoryMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class MvcInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefDataErrorHandling.class);
  public static final String SIZE_PARAM = "size";
  public static final String PAGE_PARAM = "page";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {
    Map<String, String[]> params = request.getParameterMap();
    Integer size = extractIntegerParam(params, SIZE_PARAM);
    Integer page = extractIntegerParam(params, PAGE_PARAM);
    LOGGER.debug("jestem tu");

    if (page != null) {
      if (size == null) {
        throw new ValidationMessageException(new Message(
            OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND_WITH_ID,
            "Size must be provided for page"));
      }
      if (size < 1) {
        throw new ValidationMessageException(new Message(
            OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND_WITH_ID,
            "Size must be provided for page"));
      }
    }

    return true;
  }

  private Integer extractIntegerParam(Map<String, String[]> params, String name) {
    return params.get(name) == null ? null : Integer.valueOf(params.get(name)[0]);
  }
}

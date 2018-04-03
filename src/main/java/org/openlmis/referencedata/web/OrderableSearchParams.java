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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.ToString;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.util.UuidUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

@ToString
public class OrderableSearchParams {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM_CODE = "program";
  private static final String ID = "id";

  private SearchParams params;

  /**
   * Wraps map of query params into an object. Remove parameters that should be managed by
   * {@link Pageable}
   */
  public OrderableSearchParams(MultiValueMap<String, Object> queryMap) {
    params = new SearchParams(queryMap);
  }

  /**
   * Gets code.
   *
   * @return String value of code or null if params doesn't contain "code" param. Empty string
   *         for null request param value.
   */
  public String getCode() {
    if (!params.containsKey(CODE)) {
      return null;
    }
    Object code = params.getFirst(CODE);
    return code == null ? "" : String.valueOf(code);
  }

  /**
   * Gets name.
   *
   * @return String value of name or null if params doesn't contain "name" param. Empty string
   *         for null request param value.
   */
  public String getName() {
    if (!params.containsKey(NAME)) {
      return null;
    }
    Object name = params.getFirst(NAME);
    return name == null ? "" : String.valueOf(name);
  }

  /**
   * Gets program code.
   *
   * @return {@link Code} value of program code or null if params doesn't contain "programCode"
   *                      param. Empty Code for request param that has no value.
   */
  public Code getProgramCode() {
    if (!params.containsKey(PROGRAM_CODE)) {
      return null;
    }
    Object programCode = params.getFirst(PROGRAM_CODE);
    return programCode == null ? Code.code("") : Code.code(String.valueOf(programCode));
  }

  /**
   * Gets and collection of {@link UUID} for "ids" key from params.
   */
  public Set<UUID> getIds() {
    return UuidUtil.getIds(params.asMultiValueMap());
  }

  /**
   * Checks if query params are valid. Returns false if any provided param is not on supported
   * list.
   */
  public boolean isValid() {
    return Collections.unmodifiableList(Arrays.asList(ID, CODE, NAME, PROGRAM_CODE))
        .containsAll(params.keySet());
  }

  public boolean isEmpty() {
    return params.isEmpty();
  }

}

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
import org.apache.commons.collections4.MapUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.util.UuidUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class OrderableSearchParams {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM_CODE = "program";
  private static final String ID = "id";

  private MultiValueMap<String, Object> params;

  /**
   * Wraps map of query params into an object. Remove parameters that should be managed by
   * {@link Pageable}
   */
  public OrderableSearchParams(MultiValueMap<String, Object> queryMap) {
    if (queryMap != null) {
      params = new LinkedMultiValueMap<>(queryMap);
      params.remove("page");
      params.remove("size");
      params.remove("sort");
    } else {
      params = new LinkedMultiValueMap<>();
    }
  }

  public String getCode() {
    Object code = params.getFirst(CODE);
    return String.valueOf(code);
  }

  public String getName() {
    Object name = params.getFirst(NAME);
    return String.valueOf(name);
  }

  public Code getProgramCode() {
    Object name = params.getFirst(PROGRAM_CODE);
    return Code.code(String.valueOf(name));
  }

  /**
   * Gets and collection of {@link UUID} for "ids" key from params.
   */
  public Set<UUID> getIds() {
    return UuidUtil.getIds(params);
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
    return MapUtils.isEmpty(params);
  }

}

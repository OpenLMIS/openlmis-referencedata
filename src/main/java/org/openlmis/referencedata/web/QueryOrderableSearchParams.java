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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_INVALID_PARAMS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.openlmis.referencedata.util.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

@ToString
public class QueryOrderableSearchParams implements OrderableRepositoryCustom.SearchParams {

  private static final String CODE = "code";
  private static final String EXACT_CODE = "exactCode";
  private static final String NAME = "name";
  private static final String PROGRAM_CODE = "program";
  private static final String TRADE_ITEM_ID = "tradeItemId";
  private static final String ID = "id";

  private static final List<String> ALL_PARAMETERS = Collections.unmodifiableList(Arrays.asList(
      ID, EXACT_CODE, CODE, NAME, PROGRAM_CODE, TRADE_ITEM_ID));

  private final SearchParams queryParams;

  /**
   * Wraps map of query params into an object. Remove parameters that should be managed by
   * {@link Pageable}
   */
  public QueryOrderableSearchParams(MultiValueMap<String, Object> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets exact orderables codes. Specifies a list of exact orderables codes to filter upon. This
   * condition precedes `code` condition.
   *
   * @return String value of code or null if params doesn't contain "code" param. Empty string for
   *     null request param value.
   * @see #getCode()
   */
  @Override
  public Set<String> getExactCodes() {
    return queryParams.getStrings(EXACT_CODE);
  }

  /**
   * Gets code.
   *
   * @return String value of code or null if params doesn't contain "code" param. Empty string
   *         for null request param value.
   * @see #getExactCodes()
   */
  @Override
  public String getCode() {
    if (!queryParams.containsKey(CODE)) {
      return null;
    }

    return defaultIfBlank(queryParams.getFirst(CODE), EMPTY);
  }

  /**
   * Gets name.
   *
   * @return String value of name or null if params doesn't contain "name" param. Empty string
   *         for null request param value.
   */
  @Override
  public String getName() {
    if (!queryParams.containsKey(NAME)) {
      return null;
    }

    return defaultIfBlank(queryParams.getFirst(NAME), EMPTY);
  }

  /**
   * Gets program codes.
   *
   * @return {@link Code} values of program codes or empty collection if params doesn't contain
   *         "programCodes" param. No program code is included if a program code
   *         in request param is blank.
   */
  @Override
  public Set<String> getProgramCodes() {
    return queryParams.getStrings(PROGRAM_CODE);
  }

  @Override
  public Set<Pair<UUID, Long>> getIdentityPairs() {
    return Collections.emptySet();
  }

  @Override
  public Set<UUID> getTradeItemId() {
    return queryParams.getUuids(TRADE_ITEM_ID);
  }

  /**
   * Gets and collection of {@link UUID} for "ids" key from params.
   */
  public Set<UUID> getIds() {
    if (!queryParams.containsKey(ID)) {
      return Collections.emptySet();
    }
    return queryParams.getUuids(ID);
  }

  /**
   * Check if all params are empty.
   */
  public boolean isEmpty() {
    return queryParams.isEmpty();
  }

  private void validate() {
    if (!ALL_PARAMETERS.containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
  }
}

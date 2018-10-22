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

import static java.util.Arrays.asList;
import static org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys.ERROR_LACK_PARAMS;

import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.springframework.util.MultiValueMap;

@EqualsAndHashCode
@ToString
public final class FacilityTypeApprovedProductSearchParams {

  private static final String FACILITY_TYPE = "facilityType";
  private static final String PROGRAM = "program";

  private static final List<String> ALL_PARAMETERS = asList(FACILITY_TYPE, PROGRAM);

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  public FacilityTypeApprovedProductSearchParams(MultiValueMap<String, Object> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets {@link List} of {@link String} for "facilityType" key from params.
   *
   * @return list of string values of facility type codes
   * @throws ValidationMessageException if params doesn't contain at least one "facilityType" key.
   */
  public List<String> getFacilityTypeCodes() {
    if (!queryParams.containsKey(FACILITY_TYPE)) {
      throw new ValidationMessageException(ERROR_LACK_PARAMS);
    }

    return queryParams
        .get(FACILITY_TYPE)
        .stream()
        .map(String::valueOf)
        .collect(Collectors.toList());
  }

  /**
   * Gets {@link String} for "program" key from params.
   *
   * @return String value of program or null if params doesn't contain "program" key.
   */
  public String getProgram() {
    if (!queryParams.containsKey(PROGRAM)) {
      return null;
    }
    return queryParams.getFirst(PROGRAM);
  }

  /**
   * Checks if query params are valid. Returns false if any provided param is not on supported list.
   */
  public void validate() {
    if (!ALL_PARAMETERS.containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
  }
}

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
import static org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys.ERROR_INVALID_PARAMS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.openlmis.referencedata.util.Message;
import org.springframework.util.MultiValueMap;

@EqualsAndHashCode
@ToString
public final class FacilitySearchParams implements FacilityRepositoryCustom.SearchParams {

  private static final String ID = "id";
  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String FACILITY_TYPE_CODE = "type";
  private static final String ZONE_ID = "zoneId";
  private static final String RECURSE = "recurse";
  private static final String EXTRA_DATA = "extraData";
  private static final String EXCLUDE_WARDS_SERVICES = "excludeWardsServices";
  private static final String ACTIVE = "active";

  private static final List<String> ALL_PARAMETERS =
      asList(CODE, NAME, FACILITY_TYPE_CODE, ZONE_ID, RECURSE, EXTRA_DATA, ID,
          EXCLUDE_WARDS_SERVICES, ACTIVE);

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  public FacilitySearchParams(MultiValueMap<String, Object> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets {@link String} for "code" key from params.
   *
   * @return String value of code or null if params doesn't contain "code" key.
   */
  public String getCode() {
    if (!queryParams.containsKey(CODE)) {
      return null;
    }
    return queryParams.getFirst(CODE);
  }

  /**
   * Gets {@link String} for "name" key from params.
   *
   * @return String value of name or null if params doesn't contain "name" key.
   */
  public String getName() {
    if (!queryParams.containsKey(NAME)) {
      return null;
    }
    return queryParams.getFirst(NAME);
  }

  /**
   * Gets {@link String} for "type" key from params.
   *
   * @return String value of Facility Type code or null if params doesn't contain "type" key.
   */
  public String getFacilityTypeCode() {
    if (!queryParams.containsKey(FACILITY_TYPE_CODE)) {
      return null;
    }
    return queryParams.getFirst(FACILITY_TYPE_CODE);
  }

  /**
   * Gets geographic zone id.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return UUID value of zone id or null if params doesn't contain "zoneId" key.
   */
  public UUID getZoneId() {
    if (!queryParams.containsKey(ZONE_ID)) {
      return null;
    }
    return queryParams.getUuid(ZONE_ID);
  }

  /**
   * Gets value for recurse parameter.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return Boolean value of recurse flag or null if params doesn't contain "recurse" key.
   */
  public Boolean isRecurse() {
    if (!queryParams.containsKey(RECURSE)) {
      return false;
    }
    return queryParams.getBoolean(RECURSE);
  }

  /**
   * Gets value for recurse parameter.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return Boolean value of recurse flag or null if params doesn't contain "recurse" key.
   */
  public Map getExtraData() {
    if (!queryParams.containsKey(EXTRA_DATA)) {
      return null;
    }
    return queryParams.getMap(EXTRA_DATA);
  }

  /**
   * Gets {@link Set} of {@link UUID} for "id" key from params.
   *
   * @return Set of ids from params, empty if there is no "id" param
   */
  public Set<UUID> getIds() {
    if (!queryParams.containsKey(ID)) {
      return Collections.emptySet();
    }
    return queryParams.getUuids(ID);
  }

  /**
   * Gets value for excludeWardsServices parameter.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return Boolean value of excludeWardsServices flag or false if params doesn't contain the key.
   */
  public Boolean getExcludeWardsServices() {
    if (!queryParams.containsKey(EXCLUDE_WARDS_SERVICES)) {
      return false;
    }
    return queryParams.getBoolean(EXCLUDE_WARDS_SERVICES);
  }

  /**
   * Gets value for active parameter.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return Boolean value of active flag or null if params doesn't contain "active" key.
   */
  public Boolean isActive() {
    if (!queryParams.containsKey(ACTIVE)) {
      return null;
    }
    return queryParams.getBoolean(ACTIVE);
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

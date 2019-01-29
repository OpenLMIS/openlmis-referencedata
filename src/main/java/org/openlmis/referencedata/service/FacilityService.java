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

package org.openlmis.referencedata.service;

import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isAllEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.web.FacilitySearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FacilityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityService.class);

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicZoneService geographicZoneService;

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Method returns all facilities with matched parameters. When no valid params are given,
   * returns all facilities
   *
   * @param params request parameters (code, name, zone, type, recurse) and JSON extraData.
   *               May be null or empty
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return Page of facilities. All facilities will be returned when map is null or empty
   */
  public Page<Facility> searchFacilities(FacilitySearchParams params, Pageable pageable) {
    final String code = params.getCode();
    final String name = params.getName();
    final String facilityTypeCode = params.getFacilityTypeCode();
    final UUID zoneId = params.getZoneId();
    final Boolean recurse = params.isRecurse();
    final Map extraData = params.getExtraData();
    final Set<UUID> ids = params.getIds();

    // validate query parameters
    if (isEmpty(extraData)
        && isAllEmpty(code, name, facilityTypeCode)
        && null == zoneId) {
      return facilityRepository.findAll(pageable);
    }

    // find zone if given
    if (null != zoneId && !geographicZoneRepository.exists(zoneId)) {
      throw new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    }

    // find facility type if given
    if (facilityTypeCode != null && !facilityTypeRepository.existsByCode(facilityTypeCode)) {
      throw new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    }

    Page<Facility> facilities = findFacilities(
        zoneId, code, name, facilityTypeCode, extraData, ids, recurse, pageable
    );

    return Optional.ofNullable(facilities).orElse(Pagination.getPage(Collections.emptyList()));
  }

  private Page<Facility> findFacilities(UUID zone, String code, String name,
                                        String facilityTypeCode, Map extraData, Set<UUID> ids,
                                        boolean recurse, Pageable pageable) {
    Set<UUID> zones = Sets.newHashSet();

    if (null != zone) {
      zones.add(zone);
    }

    if (recurse) {
      zones.addAll(geographicZoneService.getAllZonesInHierarchy(zone));
    }

    String extraDataString = null;

    if (isNotEmpty(extraData)) {
      try {
        extraDataString = mapper.writeValueAsString(extraData);
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
        extraDataString = null;
      }
    }

    LOGGER.info("Facility service search params: {}, {}, {}, {}, {}. {}, {}",
        code, name, zones, facilityTypeCode, extraDataString, ids, pageable);
    return facilityRepository
        .search(code, name, zones, facilityTypeCode, extraDataString, ids, pageable);
  }

}

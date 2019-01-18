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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.web.FacilitySearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class FacilityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityService.class);

  @Value("${featureFlags.facilitySearchConjunction}")
  private String facilitySearchConjunction;

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
   * Method returns all facilities with matched parameters. When no valid parameters
   * are given, returns all facilities.
   *
   * @param queryMap multi map with request parameters (id, code, name, zone, type, recurse)
   *                 and JSON extraData. There can be multiple id params,
   *                 if other params has multiple values, the first one is used.
   *                 May be null or empty
   * @return List of facilities. All facilities will be returned when map is null or empty
   */
  public List<Facility> getFacilities(MultiValueMap<String, Object> queryMap) {
    if (isEmpty(queryMap)) {
      return facilityRepository.findAll();
    }

    Set<UUID> ids = UuidUtil.getIds(queryMap);
    if (!ids.isEmpty()) {
      return facilityRepository.findAll(ids);
    }

    PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
    Page page = this.searchFacilities(new FacilitySearchParams(queryMap), pageable);

    return page.hasContent() ? page.getContent() : new ArrayList<>();
  }

  /**
   * Method returns all facilities with matched parameters. When no valid params are given,
   * returns all facilities
   *
   * @param params request parameters (code, name, zone, type, recurse) and JSON extraData.
   *               May be null or empty
   * @return List of facilities. All facilities will be returned when map is null or empty
   */
  public Page<Facility> searchFacilities(FacilitySearchParams params, Pageable pageable) {
    final String code = params.getCode();
    final String name = params.getName();
    final String facilityTypeCode = params.getFacilityTypeCode();
    final UUID zoneId = params.getZoneId();
    final Boolean recurse = params.isRecurse();
    final Map extraData = params.getExtraData();

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
        zoneId, code, name, facilityTypeCode, extraData, recurse, pageable
    );

    return facilities;
  }

  private Page<Facility> findFacilities(UUID zone, String code, String name,
                                        String facilityTypeCode, Map extraData,
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

    return facilityRepository.search(code, name, zones, facilityTypeCode, extraDataString,
        BooleanUtils.toBoolean(facilitySearchConjunction), pageable);
  }

}

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

import com.google.common.collect.Lists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.MapUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FacilityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityService.class);

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String FACILITY_TYPE_CODE = "type";
  private static final String ZONE_ID = "zoneId";
  private static final String RECURSE = "recurse";
  private static final String EXTRA_DATA = "extraData";

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

    return searchFacilities(queryMap.toSingleValueMap());
  }

  /**
   * Method returns all facilities with matched parameters. When no valid params are given,
   * returns all facilities
   *
   * @param queryMap request parameters (code, name, zone, type, recurse) and JSON extraData.
   *                 May be null or empty
   * @return List of facilities. All facilities will be returned when map is null or empty
   */
  public List<Facility> searchFacilities(Map<String, Object> queryMap) {

    String code = MapUtils.getString(queryMap, CODE, null);
    String name = MapUtils.getString(queryMap, NAME, null);
    String facilityTypeCode = MapUtils.getString(queryMap, FACILITY_TYPE_CODE, null);
    Optional<UUID> zoneId = UuidUtil.fromString(
        MapUtils.getObject(queryMap, ZONE_ID, "").toString());
    final boolean recurse = MapUtils.getBooleanValue(queryMap, RECURSE);

    // validate query parameters
    if (isEmpty(queryMap) || (isAllEmpty(code, name, facilityTypeCode) && !zoneId.isPresent())) {
      return facilityRepository.findAll();
    }

    // find zone if given
    GeographicZone zone = null;
    if (zoneId.isPresent()) {
      zone = geographicZoneRepository.findOne(zoneId.get());

      if (zone == null) {
        throw new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
      }
    }

    // find facility type if given
    if (facilityTypeCode != null && !facilityTypeRepository.existsByCode(facilityTypeCode)) {
      throw new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    }

    List<Facility> facilities = findFacilities(zone, code, name, facilityTypeCode, recurse);
    filterByExtraData(facilities, (Map) queryMap.get(EXTRA_DATA));

    return Optional.ofNullable(facilities).orElse(Collections.emptyList());
  }

  private List<Facility> findFacilities(GeographicZone zone, String code, String name,
                                        String facilityTypeCode, boolean recurse) {
    List<GeographicZone> zones = Lists.newArrayList();

    if (null != zone) {
      zones.add(zone);
    }

    if (recurse) {
      zones.addAll(geographicZoneService.getAllZonesInHierarchy(zone));
    }

    return facilityRepository.search(code, name, zones, facilityTypeCode);
  }

  private void filterByExtraData(List<Facility> foundFacilities, Map extraData) {
    if (isNotEmpty(extraData)) {
      try {
        String extraDataString = mapper.writeValueAsString(extraData);
        List<Facility> extraDataResults = facilityRepository.findByExtraData(extraDataString);

        // intersection between two lists
        foundFacilities.retainAll(extraDataResults);
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
      }
    }
  }

}

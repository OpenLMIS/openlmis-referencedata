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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
   * Method returns all facilities with matched parameters.
   *
   * @param queryMap request parameters (code, name, zone, type, recurse) and JSON extraData.
   * @return List of facilities
   */
  public List<Facility> searchFacilities(Map<String, Object> queryMap) {

    if ( MapUtils.isEmpty(queryMap) ) {
      return Lists.newArrayList(facilityRepository.findAll());
    }

    String code = MapUtils.getString(queryMap, CODE, null);
    String name = MapUtils.getString(queryMap, NAME, null);
    String facilityTypeCode = MapUtils.getString(queryMap, FACILITY_TYPE_CODE, null);
    Optional<UUID> zoneId = UuidUtil.fromString(MapUtils.getObject(queryMap,
        ZONE_ID,
        "").toString());
    final boolean recurse = MapUtils.getBooleanValue(queryMap, RECURSE);

    // validate query parameters
    if (StringUtils.isEmpty(code)
        && StringUtils.isEmpty(name)
        && StringUtils.isEmpty(facilityTypeCode)
        && !zoneId.isPresent()) {

      throw new ValidationMessageException(
          FacilityMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
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
    FacilityType facilityType = null;
    if (facilityTypeCode != null) {
      facilityType = facilityTypeRepository.findOneByCode(facilityTypeCode);
      if (facilityType == null) {
        throw new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
      }
    }

    List<Facility> foundFacilities = findFacilitiesBasedOnZone(zone, code, name,
            facilityType, recurse);

    foundFacilities = filterByExtraData(foundFacilities,
        (Map<String, String>) queryMap.get(EXTRA_DATA));

    return Optional.ofNullable(foundFacilities).orElse(Collections.emptyList());
  }

  /**
   * Method returns all facilities within the geographic zone (non-recursive).
   *
   * @param zone requested geographic zone.
   * @return List of facilities
   */
  public List<Facility> findFacilitiesBasedOnlyOnZone(GeographicZone zone) {
    return findFacilitiesBasedOnZone(zone, null, null, null, false);
  }

  private List<Facility> findFacilitiesBasedOnZone(GeographicZone zone, String code, String name,
                                                   FacilityType facilityType, boolean recurse) {
    List<Facility> foundFacilities = new ArrayList<>();

    if (recurse) {
      Collection<GeographicZone> foundZones = geographicZoneService.getAllZonesInHierarchy(zone);
      foundZones.add(zone);

      for (GeographicZone foundZone : foundZones) {
        foundFacilities.addAll(facilityRepository.search(code, name, foundZone, facilityType));
      }
    } else {
      foundFacilities.addAll(facilityRepository.search(code, name, zone, facilityType));
    }

    return foundFacilities;
  }

  private List<Facility> filterByExtraData(List<Facility> foundFacilities,
                                           Map<String, String> extraData) {

    if (extraData != null && !extraData.isEmpty()) {
      String extraDataString;
      try {
        extraDataString = mapper.writeValueAsString(extraData);
        List<Facility> extraDataResults = facilityRepository.findByExtraData(extraDataString);

        if (foundFacilities != null) {
          // intersection between two lists
          foundFacilities.retainAll(extraDataResults);
        } else {
          foundFacilities = extraDataResults;
        }
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
      }
    }

    return foundFacilities;
  }


}

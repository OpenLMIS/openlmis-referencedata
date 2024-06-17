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

import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.openlmis.referencedata.service.FacilityTypeService.WARD_SERVICE_TYPE_CODE;
import static org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys.ERROR_SHOULD_BE_ONE_NON_WARD_SERVICE_FACILITY;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.SupportedProgramPrimaryKey;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.web.FacilitySearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
   * @param params   request parameters (code, name, zone, type, recurse) and JSON extraData.
   *                 May be null or empty
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return Page of facilities. All facilities will be returned when map is null or empty
   */
  public Page<Facility> searchFacilities(FacilitySearchParams params, Pageable pageable) {
    Profiler profiler = new Profiler("FACILITY_SERVICE_SEARCH");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_IF_GEO_ZONE_EXISTS");
    if (null != params.getZoneId() && !geographicZoneRepository.existsById(params.getZoneId())) {
      throw new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    }

    profiler.start("CHECK_IF_FACILITY_TYPE_EXISTS");
    if (params.getFacilityTypeCode() != null
        && !facilityTypeRepository.existsByCode(params.getFacilityTypeCode())) {
      throw new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    }

    Page<Facility> facilities = findFacilities(params, pageable, profiler);

    profiler.stop().log();
    return facilities;
  }

  /**
   * Add configurations for newly added Ward/Service.
   *
   * @param facility Ward/Service to update configuration
   */
  @Transactional(propagation = REQUIRES_NEW)
  public void addWardServiceConfiguration(Facility facility) {
    Facility mainFacility = findMainFacilityForWardService(facility);
    facilityRepository.saveAndFlush(migrateSupportedPrograms(facility, mainFacility));
  }

  /**
   * Searches for a main facility for ward/service.
   *
   * @param facility Ward/Service for which the main facility is sought
   * @return The main facility.
   */
  public Facility findMainFacilityForWardService(Facility facility) {
    GeographicZone zone = facility.getGeographicZone();
    List<Facility> facilities = facilityRepository.findByGeographicZone(zone);
    List<Facility> facilityList = facilities.stream()
        .filter(facilityItem -> !facilityItem.getType().getCode().equals(WARD_SERVICE_TYPE_CODE))
        .collect(Collectors.toList());
    if (facilityList.size() != 1) {
      throw new ValidationMessageException(
          new Message(ERROR_SHOULD_BE_ONE_NON_WARD_SERVICE_FACILITY));
    }
    return facilityList.get(0);
  }

  private Facility migrateSupportedPrograms(Facility facility, Facility mainFacility) {
    Set<SupportedProgram> supportedProgramList = new HashSet<>();
    supportedProgramList.addAll(facility.getSupportedPrograms());
    supportedProgramList.addAll(mainFacility.getSupportedPrograms());

    List<SupportedProgram> modifiedSupportedProgramList = supportedProgramList.stream()
        .map(supportedProgram -> createSupportedProgram(supportedProgram, facility))
        .collect(Collectors.toList());

    facility.removeAllSupportedPrograms();
    modifiedSupportedProgramList.forEach(facility::addSupportedProgram);
    return facility;
  }

  private SupportedProgram createSupportedProgram(SupportedProgram supportedProgram,
                                                  Facility facility) {
    Program program = supportedProgram.getFacilityProgram().getProgram();
    SupportedProgramPrimaryKey primaryKey = new SupportedProgramPrimaryKey(facility, program);

    return new SupportedProgram(
        primaryKey, supportedProgram.getActive(),
        supportedProgram.getLocallyFulfilled(),
        supportedProgram.getStartDate());
  }

  private Page<Facility> findFacilities(FacilitySearchParams params, Pageable pageable,
                                        Profiler profiler) {

    profiler.start("GET_GEOGRAPHIC_ZONES");
    Set<UUID> zones = Sets.newHashSet();
    if (null != params.getZoneId()) {
      zones.add(params.getZoneId());
      if (params.isRecurse()) {
        profiler.start("GET_ALL_ZONES_IN_HIERARCHY");
        zones.addAll(geographicZoneService.getAllZonesInHierarchy(params.getZoneId()));
      }
    }

    profiler.start("PARSE_EXTRA_DATA");
    String extraDataString = null;
    if (isNotEmpty(params.getExtraData())) {
      try {
        extraDataString = mapper.writeValueAsString(params.getExtraData());
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
        extraDataString = null;
      }
    }

    profiler.start("SEARCH_FOR_FACILITIES");
    return facilityRepository.search(params, zones, extraDataString, pageable);
  }


}

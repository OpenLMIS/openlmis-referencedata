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

import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupervisoryNodeService {

  private static final String FACILITY_ID = "facilityId";
  private static final String PROGRAM_ID = "programId";
  private static final String ZONE_ID = "zoneId";
  private static final String NAME = "name";
  private static final String CODE = "code";

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityService facilityService;

  @Autowired
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  /**
   * Method returns all supervisory nodes with matched parameters. If there is a facilityId
   * parameter, zoneId is ignored.
   *
   * @param queryMap request parameters (code, name, zoneId, facilityId, programId).
   * @return List of supervisory nodes
   */
  public Collection<SupervisoryNode> searchSupervisoryNodes(Map<String, Object> queryMap) {

    if (MapUtils.isEmpty(queryMap)) {
      return Lists.newArrayList(supervisoryNodeRepository.findAll());
    }

    String name = MapUtils.getString(queryMap, NAME, null);
    String code = MapUtils.getString(queryMap, CODE, null);
    Optional<UUID> facilityId = UuidUtil.fromString(MapUtils.getString(queryMap,
        FACILITY_ID, ""));
    Optional<UUID> zoneId = UuidUtil.fromString(MapUtils.getString(queryMap,
        ZONE_ID, ""));
    Optional<UUID> programId = UuidUtil.fromString(MapUtils.getString(queryMap,
        PROGRAM_ID, ""));

    validateQueryParameters(name, code, facilityId, zoneId, programId);

    Facility facility = findFacility(facilityId);
    List<Facility> foundFacilities = new ArrayList<>();
    if (facility == null && zoneId.isPresent()) {
      GeographicZone zone = findGeographicZone(zoneId.get());
      if (zone != null) {
        foundFacilities = facilityService.findFacilitiesBasedOnlyOnZone(zone);
      }
    }

    Program program = findProgram(programId);

    return findSupervisoryNodes(facility, program, name, code, foundFacilities);
  }

  private Set<SupervisoryNode> findSupervisoryNodeBasedOnSchedule(List<Facility> facilities,
                                                                  Program program) {
    Set<SupervisoryNode> supervisoryNodes = new HashSet<>();
    for (Facility facility : facilities) {
      supervisoryNodes.addAll(findSupervisoryNodeBasedOnSchedule(facility, program));
    }
    return supervisoryNodes;
  }

  private Set<SupervisoryNode> findSupervisoryNodeBasedOnSchedule(Facility facility,
                                                                  Program program) {
    return requisitionGroupProgramScheduleService
        .searchRequisitionGroupProgramSchedules(program, facility)
        .stream()
        .map(a -> a.getRequisitionGroup().getSupervisoryNode())
        .collect(Collectors.toSet());
  }

  private Program findProgram(Optional<UUID> programId) {
    Program program = null;
    if (programId.isPresent()) {
      program = programRepository.findOne(programId.get());
      if (program == null) {
        throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
      }
    }
    return program;
  }

  private Facility findFacility(Optional<UUID> facilityId) {
    Facility facility = null;
    if (facilityId.isPresent()) {
      facility = facilityRepository.findOne(facilityId.get());
      if (facility == null) {
        throw new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND);
      }
    }
    return facility;
  }

  private GeographicZone findGeographicZone(UUID zoneId) {
    GeographicZone zone = geographicZoneRepository.findOne(zoneId);
    if (zone == null) {
      throw new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    }
    return zone;
  }

  private void validateQueryParameters(String name, String code, Optional<UUID> facilityId,
                                       Optional<UUID> zoneId, Optional<UUID> programId) {
    if (StringUtils.isEmpty(name)
        && StringUtils.isEmpty(code)
        && !facilityId.isPresent()
        && !zoneId.isPresent()
        && !programId.isPresent()) {

      throw new ValidationMessageException(
          SupervisoryNodeMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
    }
  }

  private Collection<SupervisoryNode> findSupervisoryNodes(Facility facility, Program program,
                                                           String name, String code,
                                                           List<Facility> foundFacilities) {
    Set<SupervisoryNode> supervisoryNodes;
    if (facility != null) {
      supervisoryNodes = findSupervisoryNodeBasedOnSchedule(facility, program);
    } else {
      supervisoryNodes = findSupervisoryNodeBasedOnSchedule(foundFacilities, program);
    }

    if (name != null || code != null) {
      return supervisoryNodeRepository.search(code, name).stream()
          .filter(a -> supervisoryNodes.contains(a))
          .collect(Collectors.toSet());
    } else {
      return supervisoryNodes;
    }
  }
}

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
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.RequisitionGroupMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RequisitionGroupService {

  private static final String NAME = "name";
  private static final String CODE = "code";
  private static final String PROGRAM = "program";
  private static final String ZONE = "zone";

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Method returns all facilities with matched parameters.
   *
   * @param queryMap request parameters (code, name, zone, type, recurse) and JSON extraData.
   * @return List of facilities
   */
  public Page<RequisitionGroup> searchRequisitionGroups(Map<String, Object> queryMap,
                                                        Pageable pageable) {
    if ( MapUtils.isEmpty(queryMap) ) {
      return Pagination.getPage(Lists.newArrayList(requisitionGroupRepository.findAll()),
          pageable);
    }

    String name = MapUtils.getString(queryMap, NAME, null);
    String code = MapUtils.getString(queryMap, CODE, null);
    String zoneCode = MapUtils.getString(queryMap, ZONE, null);
    String programCode = MapUtils.getString(queryMap, PROGRAM, null);

    if (StringUtils.isEmpty(code)
        && StringUtils.isEmpty(name)
        && StringUtils.isEmpty(zoneCode)
        && StringUtils.isEmpty(programCode)) {

      throw new ValidationMessageException(
          RequisitionGroupMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
    }

    List<SupervisoryNode> supervisoryNodes = getSupervisoryNodeBasedOnZone(zoneCode);
    Program program = getProgramByCode(programCode);

    return requisitionGroupRepository.search(name, code, program, supervisoryNodes, pageable);
  }

  private List<SupervisoryNode> getSupervisoryNodeBasedOnZone(String zoneCode) {
    List<SupervisoryNode> supervisoryNodes = null;
    if (!StringUtils.isEmpty(zoneCode)) {
      GeographicZone zone = geographicZoneRepository.findByCode(zoneCode);
      if (zone == null) {
        throw new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
      }
      supervisoryNodes = supervisoryNodeRepository.search(null, null, zone);
    }
    return supervisoryNodes;
  }

  private Program getProgramByCode(String programCode) {
    Program program = null;
    if (!StringUtils.isEmpty(programCode)) {
      program = programRepository.findByCode(Code.code(programCode));
      if (program == null) {
        throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
      }
    }
    return program;
  }
}

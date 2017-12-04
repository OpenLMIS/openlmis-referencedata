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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupplyLineMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SupplyLineService {

  static final String PROGRAM = "program";
  static final String SUPERVISORY_NODE = "supervisoryNode";
  static final String SUPPLYING_FACILITY = "supplyingFacility";

  @Autowired
  private SupplyLineRepository supplyLineRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Method returns all Supply Lines with matched parameters.
   *
   * @param program           program of searched Supply Lines.
   * @param supervisoryNode   supervisoryNode of searched Supply Lines.
   * @param supplyingFacility supplyingFacility of searched Supply Lines.
   * @return list of Supply Lines with matched parameters.
   */
  public List<SupplyLine> searchSupplyLines(Program program, SupervisoryNode supervisoryNode,
                                            Facility supplyingFacility) {
    return supplyLineRepository.searchSupplyLines(program, supervisoryNode, supplyingFacility);
  }

  /**
   * Method returns page of Supply Lines with matched parameters.
   * The result will be ordered using sort parameter from pageable object.
   *
   * @param queryMap request parameters (code, name, parent, levelNumber).
   * @param pageable object with pagination parameters.
   * @return Page of Supply Lines.
   */
  public Page<SupplyLine> searchSupplyLines(Map<String, Object> queryMap, Pageable pageable) {
    if (MapUtils.isEmpty(queryMap)) {
      return supplyLineRepository.searchSupplyLines(null, null, null, pageable);
    }

    String programCode = MapUtils.getString(queryMap, PROGRAM, null);
    String supervisoryNodeCode = MapUtils.getString(queryMap, SUPERVISORY_NODE, null);
    String supplyingFacilityCode = MapUtils.getString(queryMap, SUPPLYING_FACILITY, null);

    if (StringUtils.isEmpty(programCode)
        && StringUtils.isEmpty(supervisoryNodeCode)
        && StringUtils.isEmpty(supplyingFacilityCode)) {

      throw new ValidationMessageException(
          SupplyLineMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
    }

    Program program = findProgram(programCode);
    SupervisoryNode supervisoryNode = findSupervisoryNode(supervisoryNodeCode);
    Facility supplyingFacility = findSupplyingFacility(supplyingFacilityCode);

    return supplyLineRepository.searchSupplyLines(program,
        supervisoryNode, supplyingFacility, pageable);
  }

  private Program findProgram(String programCode) {
    Program program = null;
    if (!StringUtils.isEmpty(programCode)) {
      program = programRepository.findByCode(Code.code(programCode));
      if (program == null) {
        throw new ValidationMessageException(
            new Message(ProgramMessageKeys.ERROR_NOT_FOUND));
      }
    }
    return program;
  }

  private SupervisoryNode findSupervisoryNode(String supervisoryNodeCode) {
    SupervisoryNode supervisoryNode = null;
    if (!StringUtils.isEmpty(supervisoryNodeCode)) {
      supervisoryNode = supervisoryNodeRepository.findByCode(supervisoryNodeCode);
      if (supervisoryNode == null) {
        throw new ValidationMessageException(
            new Message(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND));
      }
    }
    return supervisoryNode;
  }

  private Facility findSupplyingFacility(String supplyingFacilityCode) {
    Facility facility = null;
    if (!StringUtils.isEmpty(supplyingFacilityCode)) {
      facility = facilityRepository.findFirstByCode(supplyingFacilityCode);
      if (facility == null) {
        throw new ValidationMessageException(
            new Message(FacilityMessageKeys.ERROR_NOT_FOUND));
      }
    }
    return facility;
  }
}

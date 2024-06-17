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

package org.openlmis.referencedata.service.stockmanagement;

import java.util.UUID;
import org.openlmis.referencedata.domain.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidSourceDestinationService {

  @Autowired
  private ValidSourceDestinationStockManagementService validAssignmentService;

  /**
   * Creates valid destinations and sources assignments for ward/service with type based on
   * main facility.
   *
   * @param mainFacility Main facility for the ward/service.
   * @param wardServiceId Ward/Service ID for which the assignments will be created.
   */
  public void addValidAssignments(Facility mainFacility, UUID wardServiceId) {
    ValidAssignmentDto assignment = new ValidAssignmentDto();
    assignment.setFacilityTypeId(mainFacility.getType().getId());
    Node node = new Node();
    node.setRefDataFacility(true);
    node.setReferenceId(wardServiceId);
    assignment.setNode(node);
    mainFacility.getSupportedPrograms().forEach(
        program -> {
          assignment.setProgramId(program.programId());
          validAssignmentService.createDestination(assignment);
          validAssignmentService.createSource(assignment);
        }
    );
  }

}

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


package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.web.csv.model.ImportField;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoleAssignmentImportDto {

  @ImportField(name = "username")
  private String username;

  @ImportField(name = "roleName")
  private String roleName;

  @ImportField(name = "type")
  private String type;

  @ImportField(name = "programCode")
  private String programCode;

  @ImportField(name = "warehouseCode")
  private String warehouseCode;

  @ImportField(name = "supervisoryNodeCode")
  private String supervisoryNodeCode;

  /**
   * Maps list of role assignments to list of RoleAssignmentImportDto objects.
   *
   * @param roleAssignments role assignments list
   * @return list of {@link RoleAssignmentImportDto} objects
   */
  public static List<RoleAssignmentImportDto> mapToDto(List<RoleAssignment> roleAssignments) {
    return roleAssignments.stream()
        .map(RoleAssignmentImportDto::convertToDto)
        .collect(Collectors.toList());
  }

  private static RoleAssignmentImportDto convertToDto(RoleAssignment roleAssignment) {
    RoleAssignmentImportDto dto = new RoleAssignmentImportDto();
    dto.setUsername(roleAssignment.getUser().getUsername());
    dto.setRoleName(roleAssignment.getRole().getName());
    dto.setType(roleAssignment.getType());

    if (roleAssignment instanceof FulfillmentRoleAssignment) {
      FulfillmentRoleAssignment fulfilmentRole = (FulfillmentRoleAssignment) roleAssignment;
      if (fulfilmentRole.getWarehouse() != null) {
        dto.setWarehouseCode(fulfilmentRole.getWarehouse().getCode());
      }
    }

    if (roleAssignment instanceof SupervisionRoleAssignment) {
      SupervisionRoleAssignment supervisoryRole = (SupervisionRoleAssignment) roleAssignment;
      if (supervisoryRole.getProgram() != null) {
        dto.setProgramCode(supervisoryRole.getProgram().getCode().toString());
      }
      if (supervisoryRole.getSupervisoryNode() != null) {
        dto.setSupervisoryNodeCode(supervisoryRole.getSupervisoryNode().getCode());
      }
    }

    return dto;
  }
}

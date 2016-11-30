package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedRoleAssignmentDto implements RoleAssignment.DetailedExporter,
    SupervisionRoleAssignment.DetailedExporter, FulfillmentRoleAssignment.DetailedExporter {

  @Getter
  private RoleDto role;

  @Getter
  @Setter
  private String programCode;

  @Getter
  @Setter
  private String supervisoryNodeCode;

  @Getter
  @Setter
  private String warehouseCode;

  @Override
  public void setRole(Role role) {
    RoleDto roleDto = new RoleDto();
    role.export(roleDto);
    this.role = roleDto;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DetailedRoleAssignmentDto)) {
      return false;
    }
    DetailedRoleAssignmentDto that = (DetailedRoleAssignmentDto) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(programCode, that.programCode)
        && Objects.equals(supervisoryNodeCode, that.supervisoryNodeCode)
        && Objects.equals(warehouseCode, that.warehouseCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, programCode, supervisoryNodeCode, warehouseCode);
  }
}


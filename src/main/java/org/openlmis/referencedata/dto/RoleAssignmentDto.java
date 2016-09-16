package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;

import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleAssignmentDto implements RoleAssignment.Exporter,
    SupervisionRoleAssignment.Exporter, FulfillmentRoleAssignment.Exporter {

  @Getter
  @Setter
  UUID roleId;

  @Getter
  @Setter
  String programCode;

  @Getter
  @Setter
  String supervisoryNodeCode;

  @Getter
  @Setter
  String warehouseCode;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RoleAssignmentDto)) {
      return false;
    }
    RoleAssignmentDto that = (RoleAssignmentDto) obj;
    return Objects.equals(roleId, that.roleId)
        && Objects.equals(programCode, that.programCode)
        && Objects.equals(supervisoryNodeCode, that.supervisoryNodeCode)
        && Objects.equals(warehouseCode, that.warehouseCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, programCode, supervisoryNodeCode, warehouseCode);
  }
}


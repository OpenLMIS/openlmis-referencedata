package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;

import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedRoleAssignmentDto implements RoleAssignment.Exporter,
    SupervisionRoleAssignment.Exporter, FulfillmentRoleAssignment.Exporter {

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

  @Getter
  @Setter
  private UUID programId;

  @Getter
  @Setter
  private UUID supervisoryNodeId;

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
        && Objects.equals(warehouseCode, that.warehouseCode)
        && Objects.equals(warehouseCode, that.warehouseCode)
        && Objects.equals(programId, that.programId)
        && Objects.equals(supervisoryNodeId, that.supervisoryNodeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, programCode, supervisoryNodeCode, warehouseCode,
        programId, supervisoryNodeId);
  }

  @Override
  public void setProgram(Program program) {
    this.programId = program.getId();
    this.programCode = program.getCode().toString();
  }

  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    this.supervisoryNodeId = supervisoryNode.getId();
    this.supervisoryNodeCode = supervisoryNode.getCode();
  }
}


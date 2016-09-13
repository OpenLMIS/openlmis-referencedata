package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;

import java.util.UUID;

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
}


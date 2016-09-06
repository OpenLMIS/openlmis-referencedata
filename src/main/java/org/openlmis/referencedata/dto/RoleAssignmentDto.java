package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class RoleAssignmentDto {
  
  @Getter
  @Setter    
  UUID roleId;

  @Getter
  @Setter
  String programCode;

  @Getter
  @Setter
  UUID supervisoryNodeId;
  
  @Getter
  @Setter
  String warehouseCode;
}

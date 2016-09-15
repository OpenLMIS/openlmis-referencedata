package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.SupplyLine;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplyLineDto {
  private UUID id;
  private UUID supervisoryNode;
  private String description;
  private UUID program;
  private UUID supplyingFacility;

  public SupplyLineDto(SupplyLine supplyLine) {
    id = supplyLine.getId();
    supervisoryNode = supplyLine.getSupervisoryNode().getId();
    description = supplyLine.getDescription();
    program = supplyLine.getProgram().getId();
    supplyingFacility = supplyLine.getSupplyingFacility().getId();
  }
}

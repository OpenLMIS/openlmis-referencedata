package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.SupplyLine;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SupplyLineSimpleDto {
  private UUID id;
  private UUID supervisoryNode;
  private String description;
  private UUID program;
  private UUID supplyingFacility;

  /**
   * Converts SupplyLine to SupplyLineDto.
   * @param supplyLine SupplyLine to convert.
   */
  public SupplyLineSimpleDto(SupplyLine supplyLine) {
    id = supplyLine.getId();
    supervisoryNode = supplyLine.getSupervisoryNode().getId();
    description = supplyLine.getDescription();
    program = supplyLine.getProgram().getId();
    supplyingFacility = supplyLine.getSupplyingFacility().getId();
  }
}

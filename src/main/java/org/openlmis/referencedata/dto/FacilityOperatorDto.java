package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.FacilityOperator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FacilityOperatorDto extends BaseDto
    implements FacilityOperator.Exporter, FacilityOperator.Importer {
  private String code;
  private String name;
  private String description;
  private Integer displayOrder;
}

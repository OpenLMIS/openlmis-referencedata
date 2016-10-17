package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.FacilityType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FacilityTypeDto extends BaseDto
    implements FacilityType.Exporter, FacilityType.Importer {
  private String code;
  private String name;
  private String description;
  private Integer displayOrder;
  private Boolean active;
}

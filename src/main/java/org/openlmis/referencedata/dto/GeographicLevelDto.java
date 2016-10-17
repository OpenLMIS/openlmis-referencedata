package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.GeographicLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GeographicLevelDto extends BaseDto implements
    GeographicLevel.Exporter, GeographicLevel.Importer {
  private String code;
  private String name;
  private Integer levelNumber;
}

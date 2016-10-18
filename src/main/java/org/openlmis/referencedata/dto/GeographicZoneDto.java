package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GeographicZoneDto extends BaseDto implements
    GeographicZone.Exporter, GeographicZone.Importer {
  private String code;
  private String name;
  private GeographicLevelDto level;
  private Integer catchmentPopulation;
  private Double latitude;
  private Double longitude;

  @Override
  public void setLevel(GeographicLevel level) {
    this.level = new GeographicLevelDto();
    level.export(this.level);
  }
}

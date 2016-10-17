package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.Facility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FacilityDto extends BaseDto implements Facility.Exporter, Facility.Importer {
  private String code;
  private String name;
  private String description;
  private GeographicZoneDto geographicZone;
  private FacilityTypeDto type;
  private FacilityOperatorDto operator;
  private Boolean active;
  private Date goLiveDate;
  private Date goDownDate;
  private String comment;
  private Boolean enabled;
  private Boolean openLmisAccessible;
  private Set<ProgramDto> supportedPrograms;
}

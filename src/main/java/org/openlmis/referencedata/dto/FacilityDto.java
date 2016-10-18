package org.openlmis.referencedata.dto;

import com.google.common.collect.Sets;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
public class FacilityDto extends BaseDto implements Facility.Exporter, Facility.Importer {

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @Getter
  private GeographicZoneDto geographicZone;

  @Getter
  private FacilityTypeDto type;

  @Getter
  private FacilityOperatorDto operator;

  @Getter
  @Setter
  private Boolean active;

  @Getter
  @Setter
  private Date goLiveDate;

  @Getter
  @Setter
  private Date goDownDate;

  @Getter
  @Setter
  private String comment;

  @Getter
  @Setter
  private Boolean enabled;

  @Getter
  @Setter
  private Boolean openLmisAccessible;

  private Set<ProgramDto> supportedPrograms;

  public FacilityDto(UUID id) {
    setId(id);
  }

  @Override
  public void setGeographicZone(GeographicZone geographicZone) {
    this.geographicZone = new GeographicZoneDto();
    geographicZone.export(this.geographicZone);
  }

  @Override
  public void setType(FacilityType type) {
    this.type = new FacilityTypeDto();
    type.export(this.type);

  }

  @Override
  public void setOperator(FacilityOperator operator) {
    this.operator = new FacilityOperatorDto();
    operator.export(this.operator);
  }

  @Override
  public Set<Program.Importer> getSupportedPrograms() {
    return Sets.newHashSet(
        Optional.ofNullable(this.supportedPrograms).orElse(Collections.emptySet())
    );
  }

  @Override
  public void setSupportedPrograms(Set<SupportedProgram> supportedPrograms) {
    this.supportedPrograms = supportedPrograms
        .stream()
        .map(SupportedProgram::getProgram)
        .map(program -> {
          ProgramDto programDto = new ProgramDto();
          program.export(programDto);

          return programDto;
        })
        .collect(Collectors.toSet());
  }
}

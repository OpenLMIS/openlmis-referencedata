package org.openlmis.referencedata.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.BaseDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportedProgramDto extends BaseDto implements SupportedProgram.Exporter {

  @Getter
  private String code;

  @Getter
  private String name;

  @Getter
  private String description;

  @Getter
  private boolean programActive;

  @Getter
  private boolean periodsSkippable;

  @Getter
  private boolean showNonFullSupplyTab;

  @Getter
  private boolean supportActive;

  @Getter
  private String supportStartDate;

  @Override
  public void setFacility(Facility facility) {
  }

  @Override
  public void setProgram(Program program) {
    setId(program.getId());
    code = program.getCode().toString();
    name = program.getName();
    description = program.getDescription();
    programActive = Optional.ofNullable(program.getActive()).orElse(false);
    periodsSkippable = Optional.ofNullable(program.getPeriodsSkippable()).orElse(false);
    showNonFullSupplyTab = Optional.ofNullable(program.getShowNonFullSupplyTab()).orElse(false);
  }

  @Override
  public void setActive(boolean active) {
    supportActive = active;
  }

  @Override
  public void setStartDate(ZonedDateTime startDate) {
    supportStartDate = (startDate == null) ? null : startDate.format(
        DateTimeFormatter.ISO_LOCAL_DATE);
  }

  /**
   * Get supportStartDate from string and turn it into ZonedDateTime. Use midnight for time and UTC
   * for zone. If supportStartDate is null, return null.
   */
  @JsonIgnore
  public ZonedDateTime getZonedStartDate() {
    return (supportStartDate == null) ? null : ZonedDateTime.of(LocalDate.parse(supportStartDate),
        LocalTime.MIDNIGHT, ZoneId.of("UTC"));
  }
}

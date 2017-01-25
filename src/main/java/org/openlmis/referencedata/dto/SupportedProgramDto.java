package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

@NoArgsConstructor
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
  @Setter
  private boolean supportActive;

  @Getter
  @Setter
  private LocalDate supportStartDate;

  @Override
  public void setProgram(Program program) {
    id = program.getId();
    code = program.getCode().toString();
    name = program.getName();
    description = program.getDescription();
    programActive = Optional.ofNullable(program.getActive()).orElse(false);
    periodsSkippable = Optional.ofNullable(program.getPeriodsSkippable()).orElse(false);
    showNonFullSupplyTab = Optional.ofNullable(program.getShowNonFullSupplyTab()).orElse(false);
  }

}

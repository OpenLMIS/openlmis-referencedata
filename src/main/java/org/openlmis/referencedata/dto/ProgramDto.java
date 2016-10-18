package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.Program;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProgramDto extends BaseDto implements Program.Exporter, Program.Importer {
  private String code;
  private String name;
  private String description;
  private Boolean active;
  private Boolean periodsSkippable;
  private Boolean showNonFullSupplyTab;

  public ProgramDto(UUID id) {
    setId(id);
  }
}

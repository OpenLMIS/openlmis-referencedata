package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Code;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class ProgramDto {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private Code code;

  @Getter
  @Setter
  private String name;

}

/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.dto;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class SupportedProgramDto extends BaseDto implements SupportedProgram.Exporter {

  private String code;
  private String name;
  private String description;
  private boolean programActive;
  private boolean periodsSkippable;
  private boolean showNonFullSupplyTab;

  @Setter private boolean supportActive;

  @Setter private boolean supportLocallyFulfilled;

  @Setter private LocalDate supportStartDate;

  /**
   * Creates new instance of {@link SupportedProgramDto} based on passed supportedProgram.
   */
  public static SupportedProgramDto newInstance(SupportedProgram supportedProgram) {
    SupportedProgramDto dto = new SupportedProgramDto();
    supportedProgram.export(dto);
    return dto;
  }

  /**
   * Create new set of SupportedProgramDto based on given iterable of {@link SupportedProgram}.
   *
   * @param supportedPrograms list of {@link SupportedProgram}
   * @return new list of SupportedProgramDto.
   */
  public static List<SupportedProgramDto> newInstances(
      Iterable<SupportedProgram> supportedPrograms) {
    List<SupportedProgramDto> dtos = new LinkedList<>();
    supportedPrograms.forEach(sp -> dtos.add(newInstance(sp)));
    return dtos;
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
}

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

import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.BOOLEAN_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.FACILITY_OPERATOR_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.LOCAL_DATE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.vividsolutions.jts.geom.Point;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.web.csv.model.ImportField;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public final class FacilityDto extends BasicFacilityDto {
  @ImportField(name = "description")
  private String description;

  @ImportField(name = "operator", type = FACILITY_OPERATOR_TYPE)
  private FacilityOperatorDto operator;

  @ImportField(name = "goLiveDate", type = LOCAL_DATE)
  private LocalDate goLiveDate;

  @ImportField(name = "goDownDate", type = LOCAL_DATE)
  private LocalDate goDownDate;

  @ImportField(name = "comment")
  private String comment;

  @ImportField(name = "openLmisAccessible", type = BOOLEAN_TYPE)
  private Boolean openLmisAccessible;

  private Point location;
  private Map<String, Object> extraData;

  @JsonProperty("supportedPrograms")
  private Set<SupportedProgramDto> supportedProgramsRef;

  public FacilityDto(UUID id) {
    setId(id);
  }

  /**
   * Creates new instance of {@link FacilityDto} based on passed facility.
   */
  public static FacilityDto newInstance(Facility facility) {
    FacilityDto dto = new FacilityDto();
    facility.export(dto);

    return dto;
  }

  /**
   * Create new set of FacilityDto based on given iterable of {@link Facility}.
   *
   * @param facilities list of {@link Facility}
   * @return new list of FacilityDto.
   */
  public static List<FacilityDto> newInstances(Iterable<Facility> facilities) {
    List<FacilityDto> facilityDtos = new LinkedList<>();
    facilities.forEach(f -> facilityDtos.add(newInstance(f)));
    return facilityDtos;
  }

  @JsonSetter("operator")
  public void setOperator(FacilityOperatorDto operator) {
    this.operator = operator;
  }

  @Override
  @JsonIgnore
  public void setOperator(FacilityOperator operator) {
    this.operator = new FacilityOperatorDto();
    operator.export(this.operator);
  }
  
  @JsonIgnore
  public Set<SupportedProgramDto> getSupportedPrograms() {
    return supportedProgramsRef;
  }
  
  @Override
  @JsonIgnore
  public void setSupportedPrograms(Set<SupportedProgram> supportedPrograms) {
    if (supportedPrograms == null) {
      this.supportedProgramsRef = null;
    } else {
      this.supportedProgramsRef = supportedPrograms
          .stream()
          .map(supportedProgram -> {
            SupportedProgramDto supportedProgramDto = new SupportedProgramDto();
            supportedProgram.export(supportedProgramDto);

            return supportedProgramDto;
          })
          .collect(Collectors.toSet());
    }
  }
}

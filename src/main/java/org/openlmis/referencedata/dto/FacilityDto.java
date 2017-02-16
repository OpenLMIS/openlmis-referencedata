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

import com.fasterxml.jackson.annotation.JsonInclude;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupportedProgram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
  private LocalDate goLiveDate;
  private LocalDate goDownDate;
  private String comment;
  private Boolean enabled;
  private Boolean openLmisAccessible;

  @Getter
  private Set<SupportedProgramDto> supportedPrograms;

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
  public void setSupportedPrograms(Set<SupportedProgram> supportedPrograms) {
    this.supportedPrograms = supportedPrograms
        .stream()
        .map(supportedProgram -> {
          SupportedProgramDto supportedProgramDto = new SupportedProgramDto();
          supportedProgram.export(supportedProgramDto);

          return supportedProgramDto;
        })
        .collect(Collectors.toSet());
  }
}

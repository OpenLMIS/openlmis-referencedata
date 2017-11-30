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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class SupplyLineDto extends BaseDto implements SupplyLine.Exporter, SupplyLine.Importer {

  @JsonProperty
  @Getter
  private SupervisoryNodeBaseDto supervisoryNode;

  @Getter
  @Setter
  private String description;

  @JsonProperty
  @Getter
  private ProgramDto program;

  @JsonProperty
  @Getter
  private FacilityDto supplyingFacility;

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeDto();
      supervisoryNode.export(supervisoryNodeBaseDto);
      this.supervisoryNode = supervisoryNodeBaseDto;
    } else {
      this.supervisoryNode = null;
    }
  }

  @JsonIgnore
  @Override
  public void setProgram(Program program) {
    if (program != null) {
      ProgramDto programDto = new ProgramDto();
      program.export(programDto);
      this.program = programDto;
    } else {
      this.program = null;
    }
  }

  @JsonIgnore
  @Override
  public void setSupplyingFacility(Facility supplyingFacility) {
    if (supplyingFacility != null) {
      FacilityDto facilityDto = new FacilityDto();
      supplyingFacility.export(facilityDto);
      this.supplyingFacility = facilityDto;
    } else {
      this.supplyingFacility = null;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupplyLineDto)) {
      return false;
    }
    SupplyLineDto that = (SupplyLineDto) obj;
    return Objects.equals(getId(), that.getId())
        && Objects.equals(supervisoryNode, that.supervisoryNode)
        && Objects.equals(program, that.program)
        && Objects.equals(supplyingFacility, that.supplyingFacility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), supervisoryNode, program, supplyingFacility);
  }
}

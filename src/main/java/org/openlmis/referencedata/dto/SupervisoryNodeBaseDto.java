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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;

@NoArgsConstructor
public class SupervisoryNodeBaseDto extends BaseDto implements SupervisoryNode.Exporter,
    SupervisoryNode.Importer {

  @Getter
  @Setter
  private String code;

  @JsonProperty
  @Getter
  private FacilityDto facility;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;



  public SupervisoryNodeBaseDto(UUID id) {
    setId(id);
  }

  @JsonIgnore
  @Override
  public void setFacility(Facility facility) {
    if (facility != null) {
      FacilityDto facilityDto = new FacilityDto(facility.getId());
      facilityDto.setGeographicZone(facility.getGeographicZone());
      this.facility = facilityDto;

    } else {
      this.facility = null;
    }
  }

  public void setFacility(FacilityDto facility) {
    this.facility = facility;
  }

  @JsonIgnore
  @Override
  public void setParentNode(SupervisoryNode parentNode) {}

  @Override
  public Set<SupervisoryNode.Importer> getChildNodes() {
    return null;
  }

  @JsonIgnore
  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {}

  @JsonIgnore
  @Override
  public void setRequisitionGroup(RequisitionGroup requisitionGroup) {}

  @Override
  public SupervisoryNode.Importer getParentNode() {
    return null;
  }

  @Override
  public RequisitionGroup.Importer getRequisitionGroup() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupervisoryNodeBaseDto)) {
      return false;
    }
    SupervisoryNodeBaseDto that = (SupervisoryNodeBaseDto) obj;
    return Objects.equals(getId(), that.getId()) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), code);
  }
}

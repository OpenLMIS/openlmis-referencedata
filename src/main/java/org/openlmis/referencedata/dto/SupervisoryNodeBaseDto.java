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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupervisoryNode.Importer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupervisoryNodeBaseDto extends BaseDto implements SupervisoryNode.Exporter,
    SupervisoryNode.Importer {

  private String code;
  private FacilityDto facility;
  private String name;
  private String description;
  private Map<String, Object> extraData;

  public SupervisoryNodeBaseDto(UUID id) {
    setId(id);
  }

  @JsonSetter
  public void setFacility(FacilityDto facility) {
    this.facility = facility;
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

  @Override
  public void setParentNode(SupervisoryNode parentNode) {
    // unsupported operation
  }

  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {
    // unsupported operation
  }

  @Override
  public void setRequisitionGroup(RequisitionGroup requisitionGroup) {
    // unsupported operation
  }

  @Override
  public Importer getParentNode() {
    // unsupported operation
    return null;
  }

  @Override
  public Set<Importer> getChildNodes() {
    // unsupported operation
    return Collections.emptySet();
  }

  @Override
  public RequisitionGroup.Importer getRequisitionGroup() {
    // unsupported operation
    return null;
  }

}

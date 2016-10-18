package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;

import java.util.HashSet;
import java.util.Set;

public class SupervisoryNodeDto extends SupervisoryNodeBaseDto {

  @JsonIgnore
  @Override
  public void setFacility(Facility facility) {
    if (facility != null) {
      FacilityDto facilityDto = new FacilityDto();
      facility.export(facilityDto);
      setFacility(facilityDto);
    } else {
      setFacility((FacilityDto) null);
    }
  }

  @JsonIgnore
  @Override
  public void setParentNode(SupervisoryNode parentNode) {
    if (parentNode != null) {
      SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeBaseDto();
      parentNode.export(supervisoryNodeBaseDto);
      setParentNode(supervisoryNodeBaseDto);
    } else {
      setParentNode((SupervisoryNodeBaseDto) null);
    }
  }

  @JsonIgnore
  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {
    if (childNodes != null) {
      Set<SupervisoryNodeBaseDto> supervisoryNodeBaseDtos = new HashSet<>();

      for (SupervisoryNode childNode : childNodes) {
        SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeBaseDto();
        childNode.export(supervisoryNodeBaseDto);
        supervisoryNodeBaseDtos.add(supervisoryNodeBaseDto);
      }

      setChildNodeDtos(supervisoryNodeBaseDtos);
    } else {
      setChildNodeDtos(null);
    }
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroup(RequisitionGroup requisitionGroup) {
    if (requisitionGroup != null) {
      RequisitionGroupBaseDto requisitionGroupBaseDto = new RequisitionGroupBaseDto();
      requisitionGroup.export(requisitionGroupBaseDto);
      setRequisitionGroup(requisitionGroupBaseDto);
    } else {
      setRequisitionGroup((RequisitionGroupBaseDto) null);
    }
  }
}

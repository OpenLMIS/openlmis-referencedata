package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class SupervisoryNodeDto extends SupervisoryNodeBaseDto {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @JsonProperty
  @Getter
  private SupervisoryNodeBaseDto parentNode;

  @JsonProperty
  @Getter
  private RequisitionGroupBaseDto requisitionGroup;

  public SupervisoryNodeDto(UUID id) {
    super(id);
  }

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
      SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeDto();
      parentNode.export(supervisoryNodeBaseDto);
      setParentNode(supervisoryNodeBaseDto);
    } else {
      setParentNode((SupervisoryNodeBaseDto) null);
    }
  }

  public void setParentNode(SupervisoryNodeBaseDto parentNode) {
    this.parentNode = parentNode;
  }

  @JsonIgnore
  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {
    if (childNodes != null) {
      Set<SupervisoryNodeBaseDto> supervisoryNodeBaseDtos = new HashSet<>();

      for (SupervisoryNode childNode : childNodes) {
        SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeDto();
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

  public void setRequisitionGroup(RequisitionGroupBaseDto requisitionGroup) {
    this.requisitionGroup = requisitionGroup;
  }
}

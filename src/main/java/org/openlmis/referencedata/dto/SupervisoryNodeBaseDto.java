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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class SupervisoryNodeBaseDto extends BaseDto implements SupervisoryNode.Exporter,
    SupervisoryNode.Importer {

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @JsonProperty
  @Getter
  private FacilityDto facility;

  @JsonProperty
  @Getter
  private SupervisoryNodeBaseDto parentNode;

  @JsonProperty
  private Set<SupervisoryNodeBaseDto> childNodes;

  @JsonProperty
  @Getter
  private RequisitionGroupBaseDto requisitionGroup;

  public SupervisoryNodeBaseDto(UUID id) {
    setId(id);
  }

  @JsonIgnore
  @Override
  public void setFacility(Facility facility) {
    if (facility != null) {
      this.facility = new FacilityDto(facility.getId());
    } else {
      this.facility = null;
    }
  }

  public void setFacility(FacilityDto facility) {
    this.facility = facility;
  }

  @JsonIgnore
  @Override
  public void setParentNode(SupervisoryNode parentNode) {
    if (parentNode != null) {
      this.parentNode = new SupervisoryNodeBaseDto(parentNode.getId());
    } else {
      this.parentNode = null;
    }
  }

  public void setParentNode(SupervisoryNodeBaseDto parentNode) {
    this.parentNode = parentNode;
  }

  @Override
  public Set<SupervisoryNode.Importer> getChildNodes() {
    if (this.childNodes == null) {
      return null;
    }

    Set<SupervisoryNode.Importer> childNodes = new HashSet<>();
    childNodes.addAll(this.childNodes);
    return childNodes;
  }

  @JsonIgnore
  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {
    if (childNodes != null) {
      this.childNodes  = new HashSet<>();

      for (SupervisoryNode node : childNodes) {
        this.childNodes.add(new SupervisoryNodeBaseDto(node.getId()));
      }
    } else {
      this.childNodes = null;
    }
  }

  public void setChildNodeDtos(Set<SupervisoryNodeBaseDto> childNodes) {
    this.childNodes = childNodes;
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroup(RequisitionGroup requisitionGroup) {
    if (requisitionGroup != null) {
      this.requisitionGroup = new RequisitionGroupBaseDto(requisitionGroup.getId());
    } else {
      this.requisitionGroup = null;
    }
  }

  public void setRequisitionGroup(RequisitionGroupBaseDto requisitionGroup) {
    this.requisitionGroup = requisitionGroup;
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
    return Objects.equals(id, that.id) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code);
  }
}

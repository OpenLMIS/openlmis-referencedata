package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class RequisitionGroupBaseDto extends BaseDto implements RequisitionGroup.Importer,
    RequisitionGroup.Exporter {

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
  private SupervisoryNodeBaseDto supervisoryNode;

  @JsonProperty
  private List<RequisitionGroupProgramScheduleBaseDto> requisitionGroupProgramSchedules;

  @JsonProperty
  private Set<FacilityDto> memberFacilities;

  public RequisitionGroupBaseDto(UUID id) {
    setId(id);
  }

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      this.supervisoryNode = new SupervisoryNodeDto(supervisoryNode.getId());
    } else {
      this.supervisoryNode = null;
    }
  }

  public void setSupervisoryNode(SupervisoryNodeBaseDto supervisoryNode) {
    this.supervisoryNode = supervisoryNode;
  }

  @Override
  public List<RequisitionGroupProgramSchedule.Importer> getRequisitionGroupProgramSchedules() {
    if (requisitionGroupProgramSchedules == null) {
      return null;
    }

    List<RequisitionGroupProgramSchedule.Importer> schedules = new ArrayList<>();
    schedules.addAll(requisitionGroupProgramSchedules);
    return schedules;
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroupProgramSchedules(List<RequisitionGroupProgramSchedule> schedules) {
    if (schedules != null) {
      this.requisitionGroupProgramSchedules = new ArrayList<>();

      for (RequisitionGroupProgramSchedule schedule : schedules) {
        this.requisitionGroupProgramSchedules.add(
            new RequisitionGroupProgramScheduleBaseDto(schedule.getId()));
      }
    } else {
      this.requisitionGroupProgramSchedules = null;
    }
  }

  public void setRequisitionGroupProgramScheduleDtos(
      List<RequisitionGroupProgramScheduleBaseDto> schedules) {
    this.requisitionGroupProgramSchedules = schedules;
  }

  @Override
  public Set<Facility.Importer> getMemberFacilities() {
    if (memberFacilities == null) {
      return null;
    }

    Set<Facility.Importer> facilities = new HashSet<>();
    facilities.addAll(memberFacilities);
    return facilities;
  }

  @JsonIgnore
  @Override
  public void setMemberFacilities(Set<Facility> memberFacilities) {
    if (memberFacilities != null) {
      this.memberFacilities = new HashSet<>();

      for (Facility facility : memberFacilities) {
        this.memberFacilities.add(new FacilityDto(facility.getId()));
      }
    } else {
      this.memberFacilities = null;
    }
  }

  public void setMemberFacilityDtos(Set<FacilityDto> memberFacilities) {
    this.memberFacilities = memberFacilities;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroupBaseDto)) {
      return false;
    }
    RequisitionGroupBaseDto that = (RequisitionGroupBaseDto) obj;
    return Objects.equals(id, that.id) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code);
  }
}

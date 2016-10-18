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
import java.util.List;
import java.util.Objects;
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
  private List<RequisitionGroupProgramScheduleBaseDto> requisitionGroupProgramSchedules =
      new ArrayList<>();

  @JsonProperty
  private List<FacilityDto> memberFacilities = new ArrayList<>();

  public RequisitionGroupBaseDto(UUID id) {
    setId(id);
  }

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      this.supervisoryNode = new SupervisoryNodeBaseDto(supervisoryNode.getId());
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
  public List<Facility.Importer> getMemberFacilities() {
    if (memberFacilities == null) {
      return null;
    }

    List<Facility.Importer> facilities = new ArrayList<>();
    facilities.addAll(memberFacilities);
    return facilities;
  }

  @JsonIgnore
  @Override
  public void setMemberFacilities(List<Facility> memberFacilities) {
    if (memberFacilities != null) {
      for (Facility facility : memberFacilities) {
        this.memberFacilities.add(new FacilityDto(facility.getId()));
      }
    } else {
      this.memberFacilities = null;
    }
  }

  public void setMemberFacilityDtos(List<FacilityDto> memberFacilities) {
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

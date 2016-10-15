package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class RequisitionGroupProgramScheduleBaseDto extends BaseDto implements
      RequisitionGroupProgramSchedule.Exporter, RequisitionGroupProgramSchedule.Importer {

  @JsonProperty
  @Getter
  private RequisitionGroupBaseDto requisitionGroup;

  @Getter
  @Setter
  private Program program;

  @Getter
  @Setter
  private ProcessingSchedule processingSchedule;

  @Getter
  @Setter
  private Boolean directDelivery;

  @Getter
  @Setter
  private Facility dropOffFacility;

  public RequisitionGroupProgramScheduleBaseDto(UUID id) {
    setId(id);
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
    if (!(obj instanceof RequisitionGroupProgramScheduleBaseDto)) {
      return false;
    }
    RequisitionGroupProgramScheduleBaseDto that = (RequisitionGroupProgramScheduleBaseDto) obj;
    return Objects.equals(id, that.id) && Objects.equals(requisitionGroup, that.requisitionGroup)
        && Objects.equals(program, that.program)
        && Objects.equals(processingSchedule, that.processingSchedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, requisitionGroup, program, processingSchedule);
  }
}

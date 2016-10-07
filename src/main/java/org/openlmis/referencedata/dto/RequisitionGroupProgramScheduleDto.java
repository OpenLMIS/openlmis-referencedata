package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequisitionGroupProgramScheduleDto extends BaseDto implements
      RequisitionGroupProgramSchedule.Exporter, RequisitionGroupProgramSchedule.Importer {
  private RequisitionGroup requisitionGroup;
  private Program program;
  private ProcessingSchedule processingSchedule;
  private Boolean directDelivery;
  private Facility dropOffFacility;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroupProgramScheduleDto)) {
      return false;
    }
    RequisitionGroupProgramScheduleDto that = (RequisitionGroupProgramScheduleDto) obj;
    return Objects.equals(requisitionGroup, that.requisitionGroup)
        && Objects.equals(program, that.program)
        && Objects.equals(processingSchedule, that.processingSchedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requisitionGroup, program, processingSchedule);
  }
}

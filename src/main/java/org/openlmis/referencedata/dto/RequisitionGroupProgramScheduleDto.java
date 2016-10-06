package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequisitionGroupProgramScheduleDto extends BaseDto implements
      RequisitionGroupProgramSchedule.Exporter, RequisitionGroupProgramSchedule.Importer {
  private Program program;
  private ProcessingSchedule processingSchedule;
  private Boolean directDelivery;
  private Facility dropOffFacility;

  @Override
  public int hashCode() {
    return Objects.hash(program, dropOffFacility);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RequisitionGroupProgramScheduleDto)) {
      return false;
    }
    RequisitionGroupProgramScheduleDto object = (RequisitionGroupProgramScheduleDto) obj;
    return Objects.equals(program, object.program)
          && Objects.equals(dropOffFacility, object.dropOffFacility);
  }
}

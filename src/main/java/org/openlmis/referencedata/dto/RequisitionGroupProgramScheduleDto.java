package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequisitionGroupProgramScheduleDto extends BaseDto implements
      RequisitionGroupProgramSchedule.Exporter, RequisitionGroupProgramSchedule.Importer {
  private Program program;
  private ProcessingSchedule processingSchedule;
  private Boolean directDelivery;
  private Facility dropOffFacility;
}

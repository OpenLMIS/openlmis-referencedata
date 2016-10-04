package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

@NoArgsConstructor
@AllArgsConstructor
public class RequisitionGroupProgramScheduleDto extends BaseDto implements
      RequisitionGroupProgramSchedule.Exporter, RequisitionGroupProgramSchedule.Importer {

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
}

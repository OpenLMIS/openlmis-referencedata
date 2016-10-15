package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.referencedata.domain.RequisitionGroup;

public class RequisitionGroupProgramScheduleDto extends RequisitionGroupProgramScheduleBaseDto {

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

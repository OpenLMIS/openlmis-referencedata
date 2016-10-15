package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;

import java.util.ArrayList;
import java.util.List;

public class RequisitionGroupDto extends RequisitionGroupBaseDto {

  @JsonIgnore
  @Override
  public void setSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode != null) {
      SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeBaseDto();
      supervisoryNode.export(supervisoryNodeBaseDto);
      setSupervisoryNode(supervisoryNodeBaseDto);
    } else {
      setSupervisoryNode((SupervisoryNodeBaseDto) null);
    }
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroupProgramSchedules(List<RequisitionGroupProgramSchedule> schedules) {
    if (schedules != null) {
      List<RequisitionGroupProgramScheduleBaseDto> scheduleBaseDtos = new ArrayList<>();

      for (RequisitionGroupProgramSchedule schedule : schedules) {
        RequisitionGroupProgramScheduleBaseDto scheduleBaseDto =
            new RequisitionGroupProgramScheduleBaseDto();
        schedule.export(scheduleBaseDto);
        scheduleBaseDtos.add(scheduleBaseDto);
      }

      setRequisitionGroupProgramScheduleDtos(scheduleBaseDtos);
    } else {
      setRequisitionGroupProgramScheduleDtos(null);
    }
  }
}

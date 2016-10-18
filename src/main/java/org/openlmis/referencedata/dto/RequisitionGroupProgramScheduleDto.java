package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
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

  @JsonIgnore
  @Override
  public void setProgram(Program program) {
    if (program != null) {
      ProgramDto programDto = new ProgramDto();
      program.export(programDto);
      setProgram(programDto);
    } else {
      setProgram((ProgramDto) null);
    }
  }

  @JsonIgnore
  @Override
  public void setDropOffFacility(Facility dropOffFacility) {
    if (dropOffFacility != null) {
      FacilityDto facilityDto = new FacilityDto();
      dropOffFacility.export(facilityDto);
      setDropOffFacility(facilityDto);
    } else {
      setDropOffFacility((FacilityDto) null);
    }
  }
}

package org.openlmis.referencedata.repository.custom;


import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

public interface RequisitionGroupProgramScheduleRepositoryCustom {

  RequisitionGroupProgramSchedule searchRequisitionGroupProgramSchedule(Program program,
                                                                        Facility facility);
}

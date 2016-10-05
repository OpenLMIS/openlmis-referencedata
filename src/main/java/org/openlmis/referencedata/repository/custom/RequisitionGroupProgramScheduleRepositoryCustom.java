package org.openlmis.referencedata.repository.custom;


import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;

import java.util.List;

public interface RequisitionGroupProgramScheduleRepositoryCustom {

  List<RequisitionGroupProgramSchedule> searchRequisitionGroupProgramSchedule(
        Program program, Facility facility);
}

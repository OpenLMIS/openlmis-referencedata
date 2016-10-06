package org.openlmis.referencedata.repository.custom;


import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;

import java.util.List;

public interface RequisitionGroupProgramScheduleRepositoryCustom {

  List<RequisitionGroupProgramSchedule> searchRequisitionGroupProgramSchedule(
        Program program, Facility facility) throws RequisitionGroupProgramScheduleException;
}

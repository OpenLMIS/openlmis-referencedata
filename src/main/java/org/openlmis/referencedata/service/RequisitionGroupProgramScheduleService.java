package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequisitionGroupProgramScheduleService {

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  public RequisitionGroupProgramSchedule searchRequisitionGroupProgramSchedule(
        Program program, Facility facility) {
    return repository.searchRequisitionGroupProgramSchedule(program, facility);
  }
}

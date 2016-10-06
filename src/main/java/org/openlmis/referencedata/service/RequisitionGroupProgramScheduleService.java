package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequisitionGroupProgramScheduleService {

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  public List<RequisitionGroupProgramSchedule> searchRequisitionGroupProgramSchedule(
        Program program, Facility facility) throws RequisitionGroupProgramScheduleException {
    return repository.searchRequisitionGroupProgramSchedule(program, facility);
  }
}

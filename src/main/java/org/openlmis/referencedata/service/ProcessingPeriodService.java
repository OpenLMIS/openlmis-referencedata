package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProcessingPeriodService {

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  /**
   * Finds Periods matching all of provided parameters and
   * ordered ascending by their start date.
   * @param processingSchedule processingSchedule of searched Periods.
   * @param toDate to which day shall Period start.
   * @return list of all Periods matching all of provided parameters.
   */
  public List<ProcessingPeriod> searchPeriods(
      ProcessingSchedule processingSchedule, LocalDate toDate) {
    return periodRepository.searchPeriods(processingSchedule, toDate);
  }

  /**
   * Get Processing Periods matching all of provided parameters.
   *
   * @param program Program of searched period.
   * @param facility Facility of searched period.
   *
   * @return Collection of Processing Periods.
   */
  public List<ProcessingPeriod> filterPeriods(
        Program program, Facility facility) throws RequisitionGroupProgramScheduleException {

    List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules
          = repository.searchRequisitionGroupProgramSchedule(program, facility);

    if (requisitionGroupProgramSchedules == null || requisitionGroupProgramSchedules.isEmpty()) {
      return null;
    } else if (requisitionGroupProgramSchedules.size() != 1) {
      throw new RequisitionGroupProgramScheduleException(
            "There cannot exists more than one requisition group program schedule"
                  + " for program and facility");
    } else {
      RequisitionGroupProgramSchedule rgps = requisitionGroupProgramSchedules.get(0);
      List<ProcessingPeriod> periods
            = periodRepository.searchPeriods(rgps.getProcessingSchedule(), null);

      return periods;
    }
  }
}

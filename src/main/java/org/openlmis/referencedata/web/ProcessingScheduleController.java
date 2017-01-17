package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.dto.ProcessingScheduleDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class ProcessingScheduleController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingScheduleController.class);

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;


  @Autowired
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows creating new processingSchedules.
   *
   * @param schedule A processingSchedule bound to the request body
   * @return ResponseEntity containing the created processingSchedule
   */
  @RequestMapping(value = "/processingSchedules", method = RequestMethod.POST)
  public ResponseEntity<?> createProcessingSchedule(@RequestBody ProcessingSchedule schedule) {
    LOGGER.debug("Creating new processingSchedule");
    // Ignore provided id
    schedule.setId(null);
    scheduleRepository.save(schedule);
    return new ResponseEntity<ProcessingSchedule>(schedule, HttpStatus.CREATED);
  }

  /**
   * Allows updating processingSchedules.
   *
   * @param schedule   A processingSchedule bound to the request body
   * @param scheduleId UUID of processingSchedule which we want to update
   * @return ResponseEntity containing the updated processingSchedule
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateProcessingSchedule(@RequestBody ProcessingSchedule schedule,
                                                    @PathVariable("id") UUID scheduleId) {
    LOGGER.debug("Updating processingSchedule");
    scheduleRepository.save(schedule);
    return new ResponseEntity<>(schedule, HttpStatus.OK);
  }

  /**
   * Get all processingSchedules.
   *
   * @return ProcessingSchedules.
   */
  @RequestMapping(value = "/processingSchedules", method = RequestMethod.GET)
  public ResponseEntity<?> getAllProcessingSchedules() {
    Iterable<ProcessingSchedule> schedules = scheduleRepository.findAll();
    if (schedules == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(schedules, HttpStatus.OK);
    }
  }

  /**
   * Fetches the correct schedule, based on the provided parameters.
   *
   * @param programId the UUID of the program
   * @param facilityId the UUID of the facility
   * @return Processing Schedule for the specified parameters
   */
  @RequestMapping(value = "/processingSchedules/search", method = RequestMethod.GET)
  public ResponseEntity<?> search(@RequestParam("programId") UUID programId,
                                  @RequestParam("facilityId") UUID facilityId) {

    Program program = programRepository.findOne(programId);
    Facility facility = facilityRepository.findOne(facilityId);

    if (program == null) {
      throw new ValidationMessageException(
          new Message(ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, programId));
    }

    if (facility == null) {
      throw new ValidationMessageException(
          new Message(FacilityMessageKeys.FACILITY_NOT_FOUND_WITH_ID, facilityId));
    }

    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule =
        requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
            program, facility);

    List<ProcessingScheduleDto> schedules = new ArrayList<>();
    if (requisitionGroupProgramSchedule != null) {
      ProcessingScheduleDto scheduleDto = exportToDto(requisitionGroupProgramSchedule
          .getProcessingSchedule());
      schedules.add(scheduleDto);
    }

    return ResponseEntity.ok(schedules);
  }

  /**
   * Get chosen processingSchedule.
   *
   * @param scheduleId UUID of processingSchedule which we want to get
   * @return ProcessingSchedule.
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getProcessingSchedule(@PathVariable("id") UUID scheduleId) {
    ProcessingSchedule schedule = scheduleRepository.findOne(scheduleId);
    if (schedule == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(schedule, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting processingSchedule.
   *
   * @param scheduleId UUID of processingSchedule which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteProcessingSchedule(@PathVariable("id") UUID scheduleId) {
    ProcessingSchedule schedule = scheduleRepository.findOne(scheduleId);
    if (schedule == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      scheduleRepository.delete(schedule);
      return new ResponseEntity<ProcessingSchedule>(HttpStatus.NO_CONTENT);
    }
  }

  private ProcessingScheduleDto exportToDto(ProcessingSchedule processingSchedule) {
    ProcessingScheduleDto processingScheduleDto = new ProcessingScheduleDto();
    processingSchedule.export(processingScheduleDto);
    return processingScheduleDto;
  }
}
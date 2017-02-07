package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingScheduleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProcessingScheduleMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@Transactional
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
   * Allows creating new ProcessingSchedules.
   *
   * @param schedule a ProcessingSchedule bound to the request body.
   * @return the created ProcessingSchedule.
   */
  @RequestMapping(value = "/processingSchedules", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ProcessingSchedule createProcessingSchedule(
      @RequestBody ProcessingSchedule schedule) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    LOGGER.debug("Creating new processingSchedule");
    // Ignore provided id
    schedule.setId(null);
    scheduleRepository.save(schedule);
    return schedule;
  }

  /**
   * Allows updating ProcessingSchedules.
   *
   * @param schedule   a ProcessingSchedule bound to the request body.
   * @param scheduleId the UUID of ProcessingSchedule which we want to update.
   * @return the updated ProcessingSchedule.
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProcessingSchedule updateProcessingSchedule(
      @RequestBody ProcessingSchedule schedule, @PathVariable("id") UUID scheduleId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    LOGGER.debug("Updating processingSchedule");
    scheduleRepository.save(schedule);
    return schedule;
  }

  /**
   * Get all ProcessingSchedules.
   *
   * @return the ProcessingSchedules.
   */
  @RequestMapping(value = "/processingSchedules", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<ProcessingSchedule> getAllProcessingSchedules() {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    Iterable<ProcessingSchedule> schedules = scheduleRepository.findAll();
    if (schedules == null) {
      throw new NotFoundException(ProcessingScheduleMessageKeys.ERROR_NOT_FOUND);
    } else {
      return schedules;
    }
  }

  /**
   * Fetches the correct schedule, based on the provided parameters.
   *
   * @param programId the UUID of the program.
   * @param facilityId the UUID of the facility.
   * @return the ProcessingSchedule for the specified parameters.
   */
  @RequestMapping(value = "/processingSchedules/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<ProcessingScheduleDto> search(
      @RequestParam("programId") UUID programId, @RequestParam("facilityId") UUID facilityId) {

    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    Program program = programRepository.findOne(programId);
    Facility facility = facilityRepository.findOne(facilityId);

    if (program == null) {
      throw new ValidationMessageException(
          new Message(ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, programId));
    }

    if (facility == null) {
      throw new ValidationMessageException(
          new Message(FacilityMessageKeys.ERROR_NOT_FOUND_WITH_ID, facilityId));
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

    return schedules;
  }

  /**
   * Get chosen ProcessingSchedule.
   *
   * @param scheduleId UUID of the ProcessingSchedule which we want to get.
   * @return the ProcessingSchedule.
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProcessingSchedule getProcessingSchedule(
      @PathVariable("id") UUID scheduleId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingSchedule schedule = scheduleRepository.findOne(scheduleId);
    if (schedule == null) {
      throw new NotFoundException(ProcessingScheduleMessageKeys.ERROR_NOT_FOUND);
    } else {
      return schedule;
    }
  }

  /**
   * Allows deleting ProcessingSchedule.
   *
   * @param scheduleId UUID of the ProcessingSchedule which we want to delete.
   */
  @RequestMapping(value = "/processingSchedules/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProcessingSchedule(
      @PathVariable("id") UUID scheduleId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingSchedule schedule = scheduleRepository.findOne(scheduleId);
    if (schedule == null) {
      throw new NotFoundException(ProcessingScheduleMessageKeys.ERROR_NOT_FOUND);
    } else {
      scheduleRepository.delete(schedule);
    }
  }

  private ProcessingScheduleDto exportToDto(ProcessingSchedule processingSchedule) {
    ProcessingScheduleDto processingScheduleDto = new ProcessingScheduleDto();
    processingSchedule.export(processingScheduleDto);
    return processingScheduleDto;
  }
}
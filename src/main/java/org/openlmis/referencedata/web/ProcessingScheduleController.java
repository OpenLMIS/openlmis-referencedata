package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class ProcessingScheduleController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingScheduleController.class);

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private ProcessingPeriodService periodService;

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

  /**
   * Calculates total difference in days and months between processingSchedule beginning and end.
   *
   * @param scheduleId UUID of given processingSchedule.
   * @return String which contains information about total difference between processingSchedule
   * beginning and end.
   */
  @RequestMapping(value = "/processingSchedules/{id}/difference", method = RequestMethod.GET)
  @ResponseBody
  public String getTotalDifference(@PathVariable("id") UUID scheduleId) {
    ProcessingSchedule schedule = scheduleRepository.findOne(scheduleId);

    Iterable<ProcessingPeriod> allPeriods = periodService.searchPeriods(schedule, null);
    if (allPeriods.iterator().hasNext()) {
      ProcessingPeriod firstPeriod = allPeriods.iterator().next();
      ProcessingPeriod lastPeriod = periodRepository.findFirst1ByOrderByEndDateDesc();
      java.time.Period total = java.time.Period.between(firstPeriod.getStartDate(),
          lastPeriod.getEndDate());
      String months = Integer.toString(total.getMonths());
      String days = Integer.toString(total.getDays());

      String[] msgArgs = {months, days};
      LOGGER.debug("Returning total days and months of schedule processingPeriods");

      return messageSource.getMessage("referencedata.message.totalPeriod", msgArgs,
          LocaleContextHolder.getLocale());
    } else {
      String[] messageArgs = {"0", "0"};
      return messageSource.getMessage("referencedata.message.totalPeriod", messageArgs,
          LocaleContextHolder.getLocale());
    }
  }
}
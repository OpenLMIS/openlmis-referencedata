package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class ProcessingPeriodController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPeriodController.class);

  @Autowired @Qualifier("beforeSavePeriodValidator")
  private ProcessingPeriodValidator validator;

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private ProcessingPeriodService periodService;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  /**
   * Finds processingPeriods matching all of provided parameters.
   * @param processingSchedule processingSchedule of searched ProcessingPeriods.
   * @param toDate to which day shall ProcessingPeriod start.
   * @return ResponseEntity with list of all ProcessingPeriods matching
   *         provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/processingPeriods/search", method = RequestMethod.GET)
  public ResponseEntity<?> searchProcessingPeriods(
          @RequestParam(value = "processingSchedule", required = true)
              ProcessingSchedule processingSchedule,
          @RequestParam(value = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    List<ProcessingPeriod> result = periodService.searchPeriods(processingSchedule, toDate);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Creates given processingPeriod if possible.
   *
   * @param period ProcessingPeriod object to be created.
   * @param bindingResult Object used for validation.
   * @return ResponseEntity with created ProcessingPeriod, BAD_REQUEST otherwise.
   */
  @RequestMapping(value = "/processingPeriods", method = RequestMethod.POST)
  public ResponseEntity<?> createProcessingPeriod(@RequestBody ProcessingPeriod period,
                                        BindingResult bindingResult) {
    LOGGER.debug("Creating new processingPeriod");
    validator.validate(period, bindingResult);
    if (bindingResult.getErrorCount() == 0) {
      ProcessingPeriod newPeriod = periodRepository.save(period);
      return new ResponseEntity<ProcessingPeriod>(newPeriod, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all processingPeriods.
   *
   * @return ProcessingPeriods.
   */
  @RequestMapping(value = "/processingPeriods", method = RequestMethod.GET)
  public ResponseEntity<?> getAllProcessingPeriods() {
    Iterable<ProcessingPeriod> periods = periodRepository.findAll();
    if (periods == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(periods, HttpStatus.OK);
    }
  }

  /**
   * Allows updating processingPeriods.
   *
   * @param period A processingPeriod bound to the request body
   * @param periodId UUID of processingPeriod which we want to update
   * @return ResponseEntity containing the updated processingPeriod
   */
  @RequestMapping(value = "/processingPeriods/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateProcessingPeriod(@RequestBody ProcessingPeriod period,
                                       @PathVariable("id") UUID periodId) {
    LOGGER.debug("Updating processingPeriod");
    ProcessingPeriod updatedProcessingPeriod = periodRepository.save(period);
    return new ResponseEntity<>(updatedProcessingPeriod, HttpStatus.OK);
  }

  /**
   * Get chosen processingPeriod.
   *
   * @param periodId UUID of processingPeriod which we want to get
   * @return ProcessingPeriod.
   */
  @RequestMapping(value = "/processingPeriods/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getProcessingPeriod(@PathVariable("id") UUID periodId) {
    ProcessingPeriod period = periodRepository.findOne(periodId);
    if (period == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(period, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting processingPeriod.
   *
   * @param periodId UUID of processingPeriod which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/processingPeriods/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteProcessingPeriod(@PathVariable("id") UUID periodId) {
    ProcessingPeriod period = periodRepository.findOne(periodId);
    if (period == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      periodRepository.delete(period);
      return new ResponseEntity<ProcessingPeriod>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Returns total difference between start date and end date from given processingPeriod.
   *
   * @param periodId UUID of given processingPeriod.
   * @return String which contains information about this difference.
   */
  @RequestMapping(value = "/processingPeriods/{id}/difference", method = RequestMethod.GET)
  @ResponseBody
  public String getTotalDifference(@PathVariable("id") UUID periodId) {
    ProcessingPeriod period = periodRepository.findOne(periodId);

    java.time.Period total = java.time.Period.between(period.getStartDate(), period.getEndDate());
    String months = Integer.toString(total.getMonths());
    String days = Integer.toString(total.getDays() + 1);

    String[] msgArgs = {months, days};
    LOGGER.debug("Returning total days and months of schedule processingPeriods");

    return messageSource.getMessage("referencedata.message.totalPeriod", msgArgs,
        LocaleContextHolder.getLocale());
  }

  /**
   * Returns chosen ProcessingPeriods.
   * @param processingScheduleId processingSchedule of searched ProcessingPeriods.
   * @param startDate which day shall ProcessingPeriod start.
   * @return List of ProcessingPeriods.
   */
  @RequestMapping(value = "/processingPeriods/searchByUUIDAndDate", method = RequestMethod.GET)
  public ResponseEntity<?> searchPeriodsByUuuidAndDate(
      @RequestParam(value = "processingScheduleId", required = true) UUID processingScheduleId,
      @RequestParam(value = "startDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

    ProcessingSchedule processingSchedule =
        processingScheduleRepository.findOne(processingScheduleId);
    List<ProcessingPeriod> periods = periodService.searchPeriods(processingSchedule, startDate);
    List<ProcessingPeriodDto> result = new ArrayList<>();
    for (ProcessingPeriod period : periods) {
      ProcessingPeriodDto processingPeriodDto = new ProcessingPeriodDto(period);
      result.add(processingPeriodDto);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);

  }

}

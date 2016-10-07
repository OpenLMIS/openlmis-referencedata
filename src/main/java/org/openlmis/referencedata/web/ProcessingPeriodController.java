package org.openlmis.referencedata.web;

import com.google.common.collect.Sets;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.InvalidIdException;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
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
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Finds processingPeriods matching all of provided parameters.
   * @param programId program of searched ProcessingPeriods.
   * @param facilityId facility of searched ProcessingPeriods.
   * @return ResponseEntity with list of all ProcessingPeriods matching
   *         provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/processingPeriods/search", method = RequestMethod.GET)
  public ResponseEntity<?> searchProcessingPeriods(
        @RequestParam(value = "programId", required = true) UUID programId,
        @RequestParam(value = "facilityId", required = true) UUID facilityId)
        throws InvalidIdException, RequisitionGroupProgramScheduleException {

    if (programId == null) {
      throw new InvalidIdException("Program id must be provided.");
    }

    if (facilityId == null) {
      throw new InvalidIdException("Facility id must be provided.");
    }

    Program program = programRepository.findOne(programId);
    Facility facility = facilityRepository.findOne(facilityId);

    List<ProcessingPeriod> periods = new ArrayList<>();

    if (program != null && facility != null) {
      periods = periodService.filterPeriods(program, facility);
    }

    return ResponseEntity.ok(exportToDtos(periods));
  }

  /**
   * Create a new processing period using the provided processing period DTO.
   *
   * @param periodDto processing period DTO with which to create the processing period
   * @param bindingResult Object used for validation.
   * @return if successful, the new processing period; otherwise an HTTP error
   */
  @RequestMapping(value = "/processingPeriods", method = RequestMethod.POST)
  public ResponseEntity<?> createProcessingPeriod(@RequestBody ProcessingPeriodDto periodDto,
                                        BindingResult bindingResult) {
    ProcessingPeriod newPeriod = ProcessingPeriod.newPeriod(periodDto);
    LOGGER.debug("Creating new processingPeriod");
    validator.validate(newPeriod, bindingResult);
    if (bindingResult.getErrorCount() == 0) {
      periodRepository.save(newPeriod);

      return ResponseEntity.status(HttpStatus.CREATED).body(exportToDto(newPeriod));
    } else {
      return ResponseEntity.badRequest().body(getErrors(bindingResult));
    }
  }

  /**
   * Get all processingPeriods.
   *
   * @return ProcessingPeriods.
   */
  @RequestMapping(value = "/processingPeriods", method = RequestMethod.GET)
  public ResponseEntity<?> getAllProcessingPeriods() {
    Set<ProcessingPeriod> processingPeriods = Sets.newHashSet(periodRepository.findAll());
    Set<ProcessingPeriodDto> periodDtos = processingPeriods.stream().map(
        period -> exportToDto(period)).collect(toSet());

    return ResponseEntity.ok(periodDtos);
  }

  /**
   * Update an existing processingPeriod using the provided processingPeriod DTO.
   * Note, if the role does not exist, will create one.
   *
   * @param periodId  id of the processingPeriod to update
   * @param periodDto provided processingPeriod DTO
   * @return if successful, the updated role; otherwise an HTTP error
   */
  @RequestMapping(value = "/processingPeriods/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateProcessingPeriod(@RequestBody ProcessingPeriodDto periodDto,
                                       @PathVariable("id") UUID periodId) {
    LOGGER.debug("Updating processingPeriod");
    ProcessingPeriod updatedProcessingPeriod = ProcessingPeriod.newPeriod(periodDto);
    updatedProcessingPeriod.setId(periodId);
    periodRepository.save(updatedProcessingPeriod);
    return ResponseEntity
          .ok(exportToDto(updatedProcessingPeriod));
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
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(exportToDto(period));
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
      return ResponseEntity.notFound().build();
    } else {
      periodRepository.delete(period);
      return ResponseEntity.noContent().build();
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
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate)
        throws InvalidIdException {
    if (processingScheduleId == null) {
      throw new InvalidIdException("Processing Schedule id must be provided");
    }

    ProcessingSchedule processingSchedule =
        processingScheduleRepository.findOne(processingScheduleId);

    List<ProcessingPeriod> periods = new ArrayList<>();

    if (processingSchedule != null) {
      periods = periodService.searchPeriods(processingSchedule, startDate);
    }

    return ResponseEntity.ok(exportToDtos(periods));
  }

  private ProcessingPeriodDto exportToDto(ProcessingPeriod period) {
    ProcessingPeriodDto periodDto = new ProcessingPeriodDto();
    period.export(periodDto);
    return periodDto;
  }

  private List<ProcessingPeriodDto> exportToDtos(List<ProcessingPeriod> periods) {
    return periods.stream().map(this::exportToDto).collect(toList());
  }
}

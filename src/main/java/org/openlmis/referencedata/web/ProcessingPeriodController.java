/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toList;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.dto.ResultDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.ProcessingPeriodDtoComparator;
import org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@Transactional
public class ProcessingPeriodController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPeriodController.class);

  public static final String RESOURCE_PATH = "/processingPeriods";

  @Autowired
  @Qualifier("beforeSavePeriodValidator")
  private ProcessingPeriodValidator validator;

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProcessingPeriodService periodService;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Finds ProcessingPeriod matching all of provided parameters.
   *
   * @param programId  program of searched ProcessingPeriods.
   * @param facilityId facility of searched ProcessingPeriods.
   * @return a list of all ProcessingPeriods matching provided parameters.
   */
  @RequestMapping(value = RESOURCE_PATH + "/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<ProcessingPeriodDto> searchProcessingPeriods(
      @RequestParam(value = "programId", required = true) UUID programId,
      @RequestParam(value = "facilityId", required = true) UUID facilityId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    if (programId == null) {
      throw new ValidationMessageException(ProcessingPeriodMessageKeys.ERROR_PROGRAM_ID_NULL);
    }

    if (facilityId == null) {
      throw new ValidationMessageException(ProcessingPeriodMessageKeys.ERROR_FACILITY_ID_NULL);
    }

    Program program = programRepository.findOne(programId);
    Facility facility = facilityRepository.findOne(facilityId);

    List<ProcessingPeriod> periods = new ArrayList<>();

    if (program != null && facility != null) {
      periods = periodService.filterPeriods(program, facility);
    }

    return exportToDtos(periods);
  }

  /**
   * Create a new processing period using the provided processing period DTO.
   *
   * @param periodDto     processing period DTO with which to create the processing period
   * @param bindingResult Object used for validation.
   * @return the new processing period.
   */
  @RequestMapping(value = RESOURCE_PATH, method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ProcessingPeriodDto createProcessingPeriod(
      @RequestBody ProcessingPeriodDto periodDto, BindingResult bindingResult) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriod newPeriod = ProcessingPeriod.newPeriod(periodDto);
    LOGGER.debug("Creating new processingPeriod");
    validator.validate(newPeriod, bindingResult);
    if (bindingResult.getErrorCount() == 0) {
      periodRepository.save(newPeriod);

      return exportToDto(newPeriod);
    } else {
      ObjectError error = bindingResult.getAllErrors().get(0);
      throw new ValidationMessageException(new Message(error.getCode(), error.getArguments()));
    }
  }

  /**
   * Get all ProcessingPeriods.
   *
   * @return the ProcessingPeriods.
   */
  @RequestMapping(value = RESOURCE_PATH, method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<ProcessingPeriodDto> getAllProcessingPeriods(
      @RequestParam(value = "sort", required = false) String sort)  {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    List<ProcessingPeriodDto> periodDtos = new ArrayList<>();

    Iterable<ProcessingPeriod> processingPeriods = periodRepository.findAll();
    processingPeriods.forEach(period -> periodDtos.add(exportToDto(period)));

    if (sort != null) {
      periodDtos.sort(new ProcessingPeriodDtoComparator(sort));
    }

    return periodDtos;
  }

  /**
   * Update an existing ProcessingPeriod using the provided ProcessingPeriodDto. Note, if the role
   * does not exist, will create one.
   *
   * @param periodDto provided processing period DTO.
   * @param periodId  id of the ProcessingPeriod to update.
   * @return the updated role.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProcessingPeriodDto updateProcessingPeriod(@RequestBody ProcessingPeriodDto periodDto,
                                                    @PathVariable("id") UUID periodId,
                                                    BindingResult bindingResult) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    LOGGER.debug("Updating processingPeriod");
    ProcessingPeriod updatedProcessingPeriod = ProcessingPeriod.newPeriod(periodDto);
    updatedProcessingPeriod.setId(periodId);
    validator.validate(updatedProcessingPeriod, bindingResult);
    if (bindingResult.getErrorCount() == 0) {
      periodRepository.save(updatedProcessingPeriod);
      return exportToDto(updatedProcessingPeriod);
    } else {
      ObjectError error = bindingResult.getAllErrors().get(0);
      throw new ValidationMessageException(new Message(error.getCode(), error.getArguments()));
    }
  }

  /**
   * Get chosen ProcessingPeriodDto.
   *
   * @param periodId UUID of the ProcessingPeriodDto which we want to get
   * @return the ProcessingPeriod.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ProcessingPeriodDto getProcessingPeriod(@PathVariable("id") UUID periodId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriod period = periodRepository.findOne(periodId);
    if (period == null) {
      throw new NotFoundException(ProcessingPeriodMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportToDto(period);
    }
  }

  /**
   * Allows deleting ProcessingPeriodDto.
   *
   * @param periodId UUID of the ProcessingPeriodDto which we want to delete
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProcessingPeriod(@PathVariable("id") UUID periodId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriod period = periodRepository.findOne(periodId);
    if (period == null) {
      throw new NotFoundException(ProcessingPeriodMessageKeys.ERROR_NOT_FOUND);
    } else {
      periodRepository.delete(period);
    }
  }

  /**
   * Returns total difference between start date and end date from given ProcessingPeriod rounded to
   * whole months.
   *
   * @param periodId UUID of given ProcessingPeriod.
   * @return String which contains number of months.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}/duration", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResultDto<Integer> getDuration(@PathVariable("id") UUID periodId) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriod period = periodRepository.findOne(periodId);

    LOGGER.debug("Returning total number of months of processingPeriod");

    return new ResultDto<>(period.getDurationInMonths());
  }

  /**
   * Get the audit information related to processing period.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getProcessingPeriodAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {

    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    //Return a 404 if the specified instance can't be found
    ProcessingPeriod instance = periodRepository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(ProcessingPeriodMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(ProcessingPeriod.class, id, author, changedPropertyName, page,
        returnJson);
  }

  /**
   * Returns chosen ProcessingPeriods.
   *
   * @param processingScheduleId processing schedule of searched ProcessingPeriods.
   * @param startDate            which day shall ProcessingPeriod start.
   * @return a list of ProcessingPeriods.
   */
  @RequestMapping(value = RESOURCE_PATH + "/searchByScheduleAndDate", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<ProcessingPeriodDto> searchPeriodsByUuidAndDate(
      @RequestParam(value = "processingScheduleId", required = true) UUID processingScheduleId,
      @RequestParam(value = "startDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
    rightService.checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    if (processingScheduleId == null) {
      throw new ValidationMessageException("Processing Schedule id must be provided");
    }

    ProcessingSchedule processingSchedule =
        processingScheduleRepository.findOne(processingScheduleId);

    List<ProcessingPeriod> periods = new ArrayList<>();

    if (processingSchedule != null) {
      periods = periodService.searchPeriods(processingSchedule, startDate);
    }

    return exportToDtos(periods);
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

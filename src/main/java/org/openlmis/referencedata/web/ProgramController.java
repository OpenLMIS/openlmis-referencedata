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

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.validate.ProgramValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@Controller
@Transactional
public class ProgramController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProgramController.class);

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProgramValidator validator;

  /**
   * Allows creating a new programs.
   *
   * @param program program bound to the request body.
   * @return the created program.
   */
  @RequestMapping(value = "/programs", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Program createProgram(@RequestBody ProgramDto program,
                               BindingResult bindingResult) {
    rightService.checkAdminRight(RightName.PROGRAMS_MANAGE);

    program.setId(null);
    validator.validate(program, bindingResult);
    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }

    LOGGER.debug("Creating new program");
    // Ignore provided id
    Program newProgram = Program.newProgram(program);
    programRepository.save(newProgram);
    return newProgram;
  }

  /**
   * Get all programs.
   *
   * @return the Programs.
   */
  @RequestMapping(value = "/programs", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<Program> getAllPrograms() {
    Iterable<Program> programs = programRepository.findAll();
    if (programs == null) {
      throw new NotFoundException(ProgramMessageKeys.ERROR_NOT_FOUND);
    } else {
      return programs;
    }
  }

  /**
   * Get chosen program.
   *
   * @param programId the UUID of program which we want to get.
   * @return the Program.
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Program getChosenProgram(@PathVariable("id") UUID programId) {
    Program program = programRepository.findOne(programId);
    if (program == null) {
      throw new NotFoundException(ProgramMessageKeys.ERROR_NOT_FOUND);
    } else {
      return program;
    }
  }

  /**
   * Allows deleting program.
   *
   * @param programId UUID of the program which we want to delete.
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProgram(@PathVariable("id") UUID programId) {
    rightService.checkAdminRight(RightName.PROGRAMS_MANAGE);

    Program program = programRepository.findOne(programId);
    if (program == null) {
      throw new NotFoundException(ProgramMessageKeys.ERROR_NOT_FOUND);
    } else {
      programRepository.delete(program);
    }
  }

  /**
   * Updating Program.
   *
   * @param program the DTO class used to update program's code and name.
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Program updateProgram(@PathVariable("id") UUID id,
                               @RequestBody ProgramDto program,
                               BindingResult bindingResult) {
    rightService.checkAdminRight(RightName.PROGRAMS_MANAGE);

    if (program == null || id == null) {
      LOGGER.debug("Update failed - program id not specified");
      throw new ValidationMessageException(ProgramMessageKeys.ERROR_ID_NULL);
    }

    Program storedProgram = programRepository.findOne(id);
    if (storedProgram == null) {
      LOGGER.warn("Update failed - program with id: {} not found", id);
      throw new ValidationMessageException(
          new Message(ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, id));
    }
    if (isNotTrue(storedProgram.getCode().equals(Code.code(program.getCode())))) {
      throw new ValidationMessageException(
          new Message(ProgramMessageKeys.ERROR_CODE_IS_INVARIABLE, id));
    }

    validator.validate(program, bindingResult);
    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }

    Program updatedProgram = Program.newProgram(program);


    programRepository.save(updatedProgram);
    return updatedProgram;
  }


  /**
   * Retrieves all programs with program name similar to name parameter.
   *
   * @param programName a part of wanted Program name.
   * @return a list of wanted Programs.
   */
  @RequestMapping(value = "/programs/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Program> findProgramsByName(
      @RequestParam("name") String programName) {
    return programRepository.findProgramsByName(programName);
  }


  /**
   * Get the audit information related to program.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/programs/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getProgramAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {
    //Return a 404 if the specified instance can't be found
    Program instance = programRepository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(ProgramMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(Program.class, id, author, changedPropertyName, page,
        returnJson);
  }
}

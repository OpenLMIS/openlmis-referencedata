package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.openlmis.referencedata.repository.ProgramRepository;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class ProgramController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProgramController.class);

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Allows creating new programs.
   *
   * @param program A program bound to the request body
   * @return ResponseEntity containing the created program
   */
  @RequestMapping(value = "/programs", method = RequestMethod.POST)
  public ResponseEntity<?> createProgram(@RequestBody Program program) {
    LOGGER.debug("Creating new program");
    // Ignore provided id
    program.setId(null);
    programRepository.save(program);
    return new ResponseEntity<>(program, HttpStatus.CREATED);
  }

  /**
   * Get all programs.
   *
   * @return Programs.
   */
  @RequestMapping(value = "/programs", method = RequestMethod.GET)
  public ResponseEntity<?> getAllPrograms() {
    Iterable<Program> programs = programRepository.findAll();
    if (programs == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(programs, HttpStatus.OK);
    }
  }

  /**
   * Get chosen program.
   *
   * @param programId UUID of program which we want to get
   * @return Program.
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getChosenProgram(@PathVariable("id") UUID programId) {
    Program program = programRepository.findOne(programId);
    if (program == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(program, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting program.
   *
   * @param programId UUID of program which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteProgram(@PathVariable("id") UUID programId) {
    Program program = programRepository.findOne(programId);
    if (program == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      programRepository.delete(program);
      return new ResponseEntity<Program>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Updating Program.
   *
   * @param program DTO class used to update program's code and name
   */
  @RequestMapping(value = "/programs/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateProgram(@PathVariable("id") UUID programId,
                                         @RequestBody Program program) {
    if (program == null || programId == null) {
      LOGGER.debug("Update failed - program id not specified");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Program storedProgram = programRepository.findOne(programId);
    if (storedProgram == null) {
      LOGGER.warn("Update failed - program with id: {} not found", programId);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    programRepository.save(program);

    return new ResponseEntity<>(program, HttpStatus.OK);
  }


  /**
   * Retrieves all Programs with programName similar to name parameter.
   *
   * @param programName Part of wanted programName.
   * @return List of wanted Programs.
   */
  @RequestMapping(value = "/programs/search", method = RequestMethod.GET)
  public ResponseEntity<?> findProgramsByName(@RequestParam("name") String programName) {
    List<Program> foundPrograms = programRepository.findProgramsByName(programName);
    return new ResponseEntity<>(foundPrograms, HttpStatus.OK);
  }

  /**
   * Get stock adjustment reasons by program.
   *
   * @param programId UUID of the program.
   * @return List of stock adjustment reasons.
   */
  @RequestMapping(value = "/programs/{id}/stockAdjustmentReasons", method = RequestMethod.GET)
  public ResponseEntity<?> getStockAdjustmentReasons(@PathVariable("id") UUID programId) {
    Program program = programRepository.findOne(programId);
    if (program == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(program.getStockAdjustmentReasons(), HttpStatus.OK);
    }
  }

  /**
   * Saving stock adjustment reasons by program.
   *
   * @param stockAdjustmentReasons List of reasons bound to the request body.
   * @return ResponseEntity containing the saved reasons.
   */
  @RequestMapping(value = "/programs/{id}/stockAdjustmentReasons", method = RequestMethod.PUT)
  public ResponseEntity<?> saveStockAdjustmentReasons(
      @PathVariable("id") UUID programId,
      @RequestBody Set<StockAdjustmentReason> stockAdjustmentReasons) {
    if (programId == null) {
      LOGGER.debug("Stock adjustment reasons update failed - program id not specified");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Program program = programRepository.findOne(programId);
    if (program == null) {
      LOGGER.warn("Stock adjustment reasons update failed - program with id: {} not found",
          programId);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    program.setStockAdjustmentReasons(stockAdjustmentReasons);
    programRepository.save(program);
    return new ResponseEntity<>(program.getStockAdjustmentReasons(), HttpStatus.OK);
  }
}

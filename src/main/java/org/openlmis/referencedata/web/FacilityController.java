package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.ErrorResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class FacilityController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityController.class);

  @Autowired
  private FacilityRepository facilityRepository;
  
  @Autowired
  private ProgramRepository programRepository;
  
  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private SupplyLineService supplyLineService;

  /**
   * Allows creating new facilities.
   * If the id is specified, it will be ignored.
   *
   * @param facility A facility bound to the request body
   * @return ResponseEntity containing the created facility
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.POST)
  public ResponseEntity<?> createFacility(@RequestBody FacilityDto facility) {
    LOGGER.debug("Creating new facility");
    facility.setId(null);
    Facility newFacility = facilityRepository.save(Facility.newFacility(facility));
    LOGGER.debug("Created new facility with id: " + facility.getId());
    return ok(newFacility);
  }

  /**
   * Get all facilities.
   *
   * @return Facilities.
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.GET)
  public ResponseEntity<?> getAllFacilities() {
    Iterable<Facility> facilities = facilityRepository.findAll();
    return ok(facilities);
  }


  /**
   * Allows updating facilities.
   *
   * @param facility A facility bound to the request body
   * @param facilityId UUID of facility which we want to update
   * @return ResponseEntity containing the updated facility
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateFacilities(@RequestBody FacilityDto facility,
                                       @PathVariable("id") UUID facilityId) {

    Facility facilityToUpdate = facilityRepository.findOne(facilityId);
    if (facilityToUpdate == null) {
      facilityToUpdate = Facility.newFacility(facility);
      LOGGER.debug("Creating new facility");
    } else {
      LOGGER.debug("Updating facility with id: " + facilityId);
    }

    facilityToUpdate = facilityRepository.save(facilityToUpdate);

    LOGGER.debug("Saved facility with id: " + facilityToUpdate.getId());
    return ok(facilityToUpdate);
  }

  /**
   * Get chosen facility.
   *
   * @param facilityId UUID of facility which we want to get
   * @return Facility.
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getFacility(@PathVariable("id") UUID facilityId) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return ok(facility);
    }
  }

  /**
   * Allows deleting facility.
   *
   * @param facilityId UUID of facility which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteFacility(@PathVariable("id") UUID facilityId) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      facilityRepository.delete(facility);
      return new ResponseEntity<Facility>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Retrieves all available supplying facilities for program and supervisory node.
   *
   * @param programId program to filter facilities
   * @param supervisoryNodeId supervisoryNode to filter facilities
   * @return ResponseEntity containing matched facilities
   */
  @RequestMapping(value = "/facilities/supplying", method = RequestMethod.GET)
  public ResponseEntity<?> getSupplyingDepots(
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "supervisoryNodeId") UUID supervisoryNodeId) {
    Program program = programRepository.findOne(programId);
    SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);

    if (program == null) {
      final String errorMessage = "Given Program does not exist";
      final String errorDescription = "programId: " + programId;

      ErrorResponse errorResponse = new ErrorResponse(errorMessage, errorDescription);
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    if (supervisoryNode == null) {
      final String errorMessage = "Given SupervisorNode does not exist";
      final String errorDescription = "supervisorNodeId: " + supervisoryNodeId;

      ErrorResponse errorResponse = new ErrorResponse(errorMessage, errorDescription);
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    List<SupplyLine> supplyLines = supplyLineService.searchSupplyLines(program, 
        supervisoryNode);
    List<Facility> facilities = supplyLines.stream()
        .map(SupplyLine::getSupplyingFacility).distinct().collect(Collectors.toList());
    return ok(facilities);
  }

  /**
   * Retrieves all Facilities with facilitCode similar to code parameter or facilityName similar
   * to name parameter.
   *
   * @param code Part of wanted facility code.
   * @param name Part of wanted facility name.
   * @return List of wanted Facilities.
   */
  @RequestMapping(value = "/facilities/search",
      method = RequestMethod.GET)
  public ResponseEntity<?> findFacilitiesWithSimilarCodeOrName(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "name", required = false) String name) {
    if (code == null && name == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    List<Facility> foundFacilities =
        facilityRepository.findFacilitiesByCodeOrName(code, name);
    return ok(foundFacilities);
  }

  private ResponseEntity<FacilityDto> ok(Facility facility) {
    return new ResponseEntity<>(toDto(facility), HttpStatus.OK);
  }

  private ResponseEntity<List<FacilityDto>> ok(Iterable<Facility> facilities) {
    return new ResponseEntity<>(toDto(facilities), HttpStatus.OK);
  }

  private FacilityDto toDto(Facility facility) {
    FacilityDto dto = new FacilityDto();
    facility.export(dto);

    return dto;
  }

  private List<FacilityDto> toDto(Iterable<Facility> facilities) {
    return StreamSupport
        .stream(facilities.spliterator(), false)
        .map(this::toDto)
        .collect(Collectors.toList());
  }

}

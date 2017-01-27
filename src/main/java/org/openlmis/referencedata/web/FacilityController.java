package org.openlmis.referencedata.web;


import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.ApprovedProductDto;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Controller
@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityController.class);

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeApprovedProductRepository facilityTypeApprovedProductRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private SupplyLineService supplyLineService;


  /**
   * Allows creating new facilities. If the id is specified, it will be ignored.
   *
   * @param facilityDto A facility bound to the request body
   * @return ResponseEntity containing the created facility
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.POST)
  public ResponseEntity<FacilityDto> createFacility(@RequestBody FacilityDto facilityDto) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    LOGGER.debug("Creating new facility");
    facilityDto.setId(null);
    Facility newFacility = Facility.newFacility(facilityDto);

    boolean addSuccessful = addSupportedProgramsToFacility(
        facilityDto.getSupportedPrograms(), newFacility);
    if (!addSuccessful) {
      throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
    }

    newFacility = facilityRepository.save(newFacility);
    LOGGER.debug("Created new facility with id: ", facilityDto.getId());
    return new ResponseEntity<>(toDto(newFacility), HttpStatus.CREATED);
  }

  /**
   * Get all facilities.
   *
   * @return Facilities.
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.GET)
  public ResponseEntity<List<FacilityDto>> getAllFacilities() {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Iterable<Facility> facilities = facilityRepository.findAll();
    return ok(facilities);
  }


  /**
   * Get the audit information related to facilities.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/facilities/{id}/auditLog", method = RequestMethod.GET)
  public ResponseEntity<?> getFacilitiesAuditLog(
          @PathVariable("id") UUID id,
          @RequestParam(name = "author", required = false, defaultValue = "") String author,
          @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
                        String changedPropertyName,
          //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
          @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
                        boolean returnJson,
          Pageable page) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    //Return a 404 if the specified facility can't be found
    Facility facility = facilityRepository.findOne(id);
    if (facility == null) {
      throw new NotFoundException(FacilityMessageKeys.ERROR_NOT_FOUND);
    }

    String auditData = getAuditLog(Facility.class, id, author,
                                        changedPropertyName, page, returnJson);

    return ResponseEntity.status(HttpStatus.OK).body(auditData);
  }


  /**
   * Allows updating facilities.
   *
   * @param facilityDto A facility bound to the request body
   * @param facilityId  UUID of facility which we want to update
   * @return ResponseEntity containing the updated facility
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.PUT)
  public ResponseEntity<FacilityDto> saveFacility(
      @RequestBody FacilityDto facilityDto, @PathVariable("id") UUID facilityId) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Facility facilityToSave = Facility.newFacility(facilityDto);
    facilityToSave.setId(facilityId);

    boolean addSuccessful = addSupportedProgramsToFacility(facilityDto.getSupportedPrograms(),
        facilityToSave);
    if (!addSuccessful) {
      throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
    }
    facilityToSave = facilityRepository.save(facilityToSave);

    LOGGER.debug("Saved facility with id: " + facilityToSave.getId());
    return ok(facilityToSave);
  }

  /**
   * Get chosen facility.
   *
   * @param facilityId UUID of facility which we want to get
   * @return Facility.
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.GET)
  public ResponseEntity getFacility(@PathVariable("id") UUID facilityId) {
    
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      throw new NotFoundException(FacilityMessageKeys.ERROR_NOT_FOUND);
    } else {
      return ok(facility);
    }
  }

  /**
   * Returns full or non-full supply approved products for the given facility.
   *
   * @param facilityId ID of the facility
   * @param programId  ID of the program
   * @param fullSupply true to retrieve full-supply products, false to retrieve non-full supply
   *                   products
   * @return collection of approved products
   */
  @RequestMapping(value = "/facilities/{id}/approvedProducts")
  public ResponseEntity<List<ApprovedProductDto>> getApprovedProducts(
      @PathVariable("id") UUID facilityId,
      @RequestParam(required = false, value = "programId") UUID programId,
      @RequestParam(value = "fullSupply") boolean fullSupply) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      throw new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND);
    }

    Collection<FacilityTypeApprovedProduct> products = facilityTypeApprovedProductRepository
        .searchProducts(facilityId, programId, fullSupply);

    return ResponseEntity.ok(toDto(products));
  }

  /**
   * Allows deleting facility.
   *
   * @param facilityId UUID of facility which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteFacility(@PathVariable("id") UUID facilityId) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      throw new NotFoundException(FacilityMessageKeys.ERROR_NOT_FOUND);
    } else {
      facilityRepository.delete(facility);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Retrieves all available supplying facilities for program and supervisory node.
   *
   * @param programId         program to filter facilities
   * @param supervisoryNodeId supervisoryNode to filter facilities
   * @return ResponseEntity containing matched facilities
   */
  @RequestMapping(value = "/facilities/supplying", method = RequestMethod.GET)
  public ResponseEntity<List<FacilityDto>> getSupplyingDepots(
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "supervisoryNodeId") UUID supervisoryNodeId) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Program program = programRepository.findOne(programId);
    SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);

    if (program == null) {
      throw new ValidationMessageException(
          new Message(ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, programId));
    }
    if (supervisoryNode == null) {
      throw new ValidationMessageException(
          new Message(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND_WITH_ID, supervisoryNodeId));
    }

    List<SupplyLine> supplyLines = supplyLineService.searchSupplyLines(program, supervisoryNode);
    List<Facility> facilities = supplyLines.stream()
        .map(SupplyLine::getSupplyingFacility).distinct().collect(Collectors.toList());
    return ok(facilities);
  }

  /**
   * Retrieves all Facilities with facilitCode similar to code parameter or facilityName similar to
   * name parameter.
   *
   * @param code Part of wanted facility code.
   * @param name Part of wanted facility name.
   * @return List of wanted Facilities.
   */
  @RequestMapping(value = "/facilities/search", method = RequestMethod.GET)
  public ResponseEntity<List<FacilityDto>> findFacilitiesWithSimilarCodeOrName(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "name", required = false) String name) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    if (code == null && name == null) {
      throw new ValidationMessageException(
          FacilityMessageKeys.ERROR_SEARCH_CODE_NULL_AND_NAME_NULL);
    }
    List<Facility> foundFacilities = facilityRepository.findFacilitiesByCodeOrName(code, name);
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

  private List<ApprovedProductDto> toDto(Collection<FacilityTypeApprovedProduct> products) {
    List<ApprovedProductDto> productDtos = new ArrayList<>();
    for (FacilityTypeApprovedProduct product : products) {
      ApprovedProductDto productDto = new ApprovedProductDto();
      product.export(productDto);
      productDtos.add(productDto);
    }

    return productDtos;
  }

  private boolean addSupportedProgramsToFacility(Set<SupportedProgramDto> supportedProgramDtos,
                                                 Facility facility) {
    for (SupportedProgramDto dto : supportedProgramDtos) {
      Program program = programRepository.findByCode(Code.code(dto.getCode()));
      if (program == null) {
        LOGGER.debug("Program does not exist: ", dto.getCode());
        return false;
      }
      SupportedProgram supportedProgram = SupportedProgram.newSupportedProgram(facility,
          program, dto.isSupportActive(), dto.getSupportStartDate());
      facility.addSupportedProgram(supportedProgram);
    }

    return true;
  }
}

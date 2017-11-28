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


import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.ApprovedProductDto;
import org.openlmis.referencedata.dto.BasicFacilityDto;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.MinimalFacilityDto;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.FacilityService;
import org.openlmis.referencedata.service.RightAssignmentService;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.openlmis.referencedata.validate.FacilityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityController.class);

  public static final String RESOURCE_PATH = "/facilities";

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

  @Autowired
  private FacilityService facilityService;

  @Autowired
  private FacilityValidator facilityValidator;

  @Autowired
  private RightAssignmentService rightAssignmentService;

  /**
   * Allows creating new facilities. If the id is specified, it will be ignored.
   *
   * @param facilityDto A facility bound to the request body.
   * @return created facility.
   */
  @RequestMapping(value = RESOURCE_PATH, method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public FacilityDto createFacility(@RequestBody FacilityDto facilityDto,
                                    BindingResult bindingResult) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    facilityValidator.validate(facilityDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);

    LOGGER.debug("Creating new facility");
    facilityDto.setId(null);
    Facility newFacility = Facility.newFacility(facilityDto);

    addSupportedProgramsToFacility(facilityDto.getSupportedPrograms(), newFacility);

    newFacility = facilityRepository.save(newFacility);
    LOGGER.debug("Created new facility with id: ", facilityDto.getId());
    return toDto(newFacility);
  }

  /**
   * Get all facilities with minimal representation (id, name).
   *
   * @param pageable A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   * @return Facilities.
   */
  @RequestMapping(value = RESOURCE_PATH + "/minimal", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<MinimalFacilityDto> getMinimalFacilities(Pageable pageable) {
    Page<Facility> facilities = facilityRepository.findAll(pageable);
    List<MinimalFacilityDto> minimalFacilities = toMinimalDto(facilities.getContent());
    return Pagination.getPage(minimalFacilities, pageable);
  }


  /**
   * Get the audit information related to facilities.
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
  public ResponseEntity<String> getFacilitiesAuditLog(
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

    return getAuditLogResponse(Facility.class, id, author, changedPropertyName, page,
        returnJson);
  }


  /**
   * Allows updating facilities.
   *
   * @param facilityDto A facility bound to the request body.
   * @param facilityId UUID of facility which we want to update.
   * @return the updated facility.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityDto saveFacility(
      @RequestBody FacilityDto facilityDto,
      @PathVariable("id") UUID facilityId,
      BindingResult bindingResult) {

    Profiler profiler = new Profiler("UPDATE_FACILITY");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_ADMIN");
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    profiler.start("VALIDATE_FACILITY_DTO");
    facilityValidator.validate(facilityDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);

    profiler.start("IMPORT_FACILITY_FROM_DTO");
    Facility facilityToSave = Facility.newFacility(facilityDto);
    facilityToSave.setId(facilityId);

    profiler.start("ADD_SUPPORTED_PROGRAMS");
    addSupportedProgramsToFacility(facilityDto.getSupportedPrograms(), facilityToSave);

    profiler.start("SAVE_FACILITY");
    facilityToSave = facilityRepository.saveAndFlush(facilityToSave);

    profiler.start("REGENERATE_RIGHT_ASSIGNMENTS");
    rightAssignmentService.regenerateRightAssignments();

    LOGGER.info("Saved facility with id: {}", facilityToSave.getId());
    profiler.start("EXPORT_FACILITY_TO_DTO");
    FacilityDto dto = toDto(facilityToSave);
    
    profiler.stop().log();
    return dto;
  }

  /**
   * Get chosen facility.
   *
   * @param facilityId UUID of facility which we want to get
   * @return Facility.
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityDto getFacility(@PathVariable("id") UUID facilityId) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      throw new NotFoundException(FacilityMessageKeys.ERROR_NOT_FOUND);
    } else {
      return toDto(facility);
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
  @RequestMapping(value = RESOURCE_PATH + "/{id}/approvedProducts")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<ApprovedProductDto> getApprovedProducts(
      @PathVariable("id") UUID facilityId,
      @RequestParam(required = false, value = "programId") UUID programId,
      @RequestParam(value = "fullSupply") boolean fullSupply) {

    Profiler profiler = new Profiler("GET_FACILITY_APPROVED_PRODUCTS");
    profiler.setLogger(LOGGER);

    profiler.start("FIND_FACILITY");
    Facility facility = facilityRepository.findOne(facilityId);

    if (facility == null) {
      profiler.stop().log();
      throw new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND);
    }

    profiler.start("FIND_APPROVED_PRODUCTS");
    Collection<FacilityTypeApprovedProduct> products = facilityTypeApprovedProductRepository
        .searchProducts(facility.getType().getId(), programId, fullSupply);

    profiler.start("CONVERT_TO_DTO");
    List<ApprovedProductDto> list = toDto(products);

    profiler.stop().log();
    return list;
  }

  /**
   * Retrieves all facilities within a boundary.
   *
   * @param boundary GeoJSON polygon specifying a boundary
   * @param pageable object used to encapsulate the pagination related values: page and size.
   * @return List of wanted facilities within the boundary.
   */
  @RequestMapping(value = RESOURCE_PATH + "/byBoundary", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<FacilityDto> findFacilitiesByBoundary(@RequestBody Polygon boundary, 
      Pageable pageable) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    List<Facility> foundFacilities = facilityRepository.findByBoundary(boundary);
    List<FacilityDto> facilityDtos = toDto(foundFacilities);
    return Pagination.getPage(facilityDtos, pageable);
  }

  /**
   * Allows deleting facility.
   *
   * @param facilityId UUID of facility which we want to delete
   */
  @RequestMapping(value = RESOURCE_PATH + "/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFacility(@PathVariable("id") UUID facilityId) {

    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      throw new NotFoundException(FacilityMessageKeys.ERROR_NOT_FOUND);
    } else {
      facilityRepository.delete(facility);
    }
  }

  /**
   * Retrieves all available supplying facilities for program and supervisory node.
   *
   * @param programId         program to filter facilities.
   * @param supervisoryNodeId supervisoryNode to filter facilities.
   * @return matched facilities.
   */
  @RequestMapping(value = RESOURCE_PATH + "/supplying", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<FacilityDto> getSupplyingDepots(
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "supervisoryNodeId") UUID supervisoryNodeId) {

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
    return toDto(facilities);
  }

  /**
   * Retrieves all facilities that are matching given request parameters.
   * If no parameters, all facilities are returned.
   *
   * @param requestParams request parameters (id, code, name, zone, recurse).
   * @return List of wanted Facilities matching query parameters.
   */
  @GetMapping(value = RESOURCE_PATH)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<FacilityDto> getFacilities(
      @RequestParam MultiValueMap<String, Object> requestParams) {

    return toDto(facilityService.getFacilities(requestParams));
  }

  /**
   * Retrieves all Facilities with facilityCode similar to code parameter or facilityName similar to
   * name parameter.
   *
   * @param queryParams request parameters (code, name, zone, recurse) and JSON extraData.
   * @param pageable object used to encapsulate the pagination related values: page and size.
   * @return List of wanted Facilities matching query parameters.
   */
  @RequestMapping(value = RESOURCE_PATH + "/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<BasicFacilityDto> searchFacilities(@RequestBody Map<String, Object> queryParams,
                                                 Pageable pageable) {

    List<Facility> foundFacilities = facilityService.searchFacilities(queryParams);
    List<BasicFacilityDto> facilityDtos = toBasicDto(foundFacilities);
    return Pagination.getPage(facilityDtos, pageable);
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

  private MinimalFacilityDto toMinimalDto(Facility facility) {
    MinimalFacilityDto dto = new MinimalFacilityDto();
    facility.export(dto);

    return dto;
  }

  private List<MinimalFacilityDto> toMinimalDto(Iterable<Facility> facilities) {
    return StreamSupport
        .stream(facilities.spliterator(), false)
        .map(this::toMinimalDto)
        .collect(Collectors.toList());
  }

  private BasicFacilityDto toBasicDto(Facility facility) {
    BasicFacilityDto dto = new BasicFacilityDto();
    facility.export(dto);

    return dto;
  }

  private List<BasicFacilityDto> toBasicDto(Iterable<Facility> facilities) {
    return StreamSupport
        .stream(facilities.spliterator(), false)
        .map(this::toBasicDto)
        .collect(Collectors.toList());
  }

  private void addSupportedProgramsToFacility(Set<SupportedProgramDto> supportedProgramDtos,
                                                 Facility facility) {
    if (supportedProgramDtos != null) {
      for (SupportedProgramDto dto : supportedProgramDtos) {
        Program program;
        if (dto.getCode() != null) {
          program = programRepository.findByCode(Code.code(dto.getCode()));
        } else if (dto.getId() != null) {
          program = programRepository.findOne(dto.getId());
        } else {
          throw new ValidationMessageException(ProgramMessageKeys.ERROR_CODE_OR_ID_REQUIRED);
        }
        if (program == null) {
          throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
        }
        SupportedProgram supportedProgram = SupportedProgram.newSupportedProgram(facility,
            program, dto.isSupportActive(), dto.getSupportStartDate());
        facility.addSupportedProgram(supportedProgram);
      }
    }
  }
}

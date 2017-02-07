package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.dto.SupplyLineSimpleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.messagekeys.SupplyLineMessageKeys;
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
public class SupplyLineController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupplyLineController.class);

  @Autowired
  private SupplyLineService supplyLineService;

  @Autowired
  private SupplyLineRepository supplyLineRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows creating new supplyLines. If the id is specified, it will be ignored.
   *
   * @param supplyLineDto A supplyLine bound to the request body.
   * @return the created supplyLine.
   */
  @RequestMapping(value = "/supplyLines", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public SupplyLineDto createSupplyLine(@RequestBody SupplyLineDto supplyLineDto) {
    LOGGER.debug("Creating new supplyLine");
    supplyLineDto.setId(null);
    SupplyLine supplyLine = SupplyLine.newSupplyLine(supplyLineDto);
    supplyLineRepository.save(supplyLine);
    LOGGER.debug("Created new supplyLine with id: " + supplyLine.getId());
    return exportToDto(supplyLine);
  }

  /**
   * Get all supplyLines.
   *
   * @return the SupplyLineDtos.
   */
  @RequestMapping(value = "/supplyLines", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SupplyLineDto> getAllSupplyLines() {
    Iterable<SupplyLine> supplyLines = supplyLineRepository.findAll();
    List<SupplyLineDto> supplyLineDtos = new ArrayList<>();

    for (SupplyLine supplyLine : supplyLines) {
      supplyLineDtos.add(exportToDto(supplyLine));
    }

    return supplyLineDtos;
  }

  /**
   * Allows updating supplyLines.
   *
   * @param supplyLineDto A supplyLineDto bound to the request body.
   * @param supplyLineId  UUID of supplyLine which we want to update.
   * @return the updated supplyLine.
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyLineDto updateSupplyLine(@RequestBody SupplyLineDto supplyLineDto,
                                        @PathVariable("id") UUID supplyLineId) {

    SupplyLine supplyLineToUpdate = supplyLineRepository.findOne(supplyLineId);
    if (supplyLineToUpdate == null) {
      supplyLineToUpdate = new SupplyLine();
      LOGGER.debug("Creating new supplyLine");
    } else {
      LOGGER.debug("Updating supplyLine with id: " + supplyLineId);
    }

    supplyLineToUpdate.updateFrom(SupplyLine.newSupplyLine(supplyLineDto));
    supplyLineRepository.save(supplyLineToUpdate);

    LOGGER.debug("Saved supplyLine with id: " + supplyLineToUpdate.getId());
    return exportToDto(supplyLineToUpdate);
  }

  /**
   * Get chosen supplyLine.
   *
   * @param supplyLineId UUID of supplyLine which we want to get.
   * @return the SupplyLine.
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyLineDto getSupplyLine(@PathVariable("id") UUID supplyLineId) {
    SupplyLine supplyLine = supplyLineRepository.findOne(supplyLineId);
    if (supplyLine == null) {
      throw new NotFoundException(SupplyLineMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportToDto(supplyLine);
    }
  }

  /**
   * Allows deleting supplyLine.
   *
   * @param supplyLineId UUID of supplyLine which we want to delete
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSupplyLine(@PathVariable("id") UUID supplyLineId) {
    SupplyLine supplyLine = supplyLineRepository.findOne(supplyLineId);
    if (supplyLine == null) {
      throw new NotFoundException(SupplyLineMessageKeys.ERROR_NOT_FOUND);
    } else {
      supplyLineRepository.delete(supplyLine);
    }
  }

  /**
   * Returns all supply lines with matched parameters.
   *
   * @param programDto         program of searched Supply Lines.
   * @param supervisoryNodeDto supervisory node of searched Supply Lines.
   * @return a list of all Supply Lines matching provided parameters.
   */
  @RequestMapping(value = "/supplyLines/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SupplyLineDto> searchSupplyLines(
      @RequestParam(value = "program") ProgramDto programDto,
      @RequestParam(value = "supervisoryNode") SupervisoryNodeDto supervisoryNodeDto) {
    Program program = Program.newProgram(programDto);
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode(supervisoryNodeDto);
    List<SupplyLine> result = supplyLineService.searchSupplyLines(program, supervisoryNode);

    List<SupplyLineDto> supplyLineDtos = new ArrayList<>();

    for (SupplyLine supplyLine : result) {
      supplyLineDtos.add(exportToDto(supplyLine));
    }

    return supplyLineDtos;
  }

  /**
   * Returns all Supply Lines with matched parameters.
   *
   * @param programId         program of searched Supply Lines.
   * @param supervisoryNodeId supervisory node of searched Supply Lines.
   * @return a list of all Supply Lines matching provided parameters.
   */
  @RequestMapping(value = "/supplyLines/searchByUUID", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SupplyLineSimpleDto> searchSupplyLinesByUuid(
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "supervisoryNodeId", required = false) UUID supervisoryNodeId,
      @RequestParam(value = "supplyingFacilityId", required = false) UUID supplyingFacilityId) {
    Program program = programRepository.findOne(programId);
    SupervisoryNode supervisoryNode = null != supervisoryNodeId
        ? supervisoryNodeRepository.findOne(supervisoryNodeId)
        : null;
    Facility supplyingFacility = null != supplyingFacilityId
        ? facilityRepository.findOne(supplyingFacilityId)
        : null;


    List<SupplyLine> resultSupplyLine = supplyLineService.searchSupplyLines(
        program, supervisoryNode, supplyingFacility
    );

    List<SupplyLineSimpleDto> result = new ArrayList<>();
    for (SupplyLine supplyLine : resultSupplyLine) {
      SupplyLineSimpleDto supplyLineSimpleDto = new SupplyLineSimpleDto();
      supplyLine.export(supplyLineSimpleDto);
      result.add(supplyLineSimpleDto);
    }

    return result;
  }

  private SupplyLineDto exportToDto(SupplyLine supplyLine) {
    SupplyLineDto supplyLineDto = null;

    if (supplyLine != null) {
      supplyLineDto = new SupplyLineDto();
      supplyLine.export(supplyLineDto);
    }

    return supplyLineDto;
  }
}

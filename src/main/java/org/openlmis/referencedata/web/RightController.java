package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.repository.RightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Controller
public class RightController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);

  @Autowired
  private RightRepository rightRepository;

  public RightController(RightRepository repository) {
    super();
    this.rightRepository = Objects.requireNonNull(repository);
  }

  /**
   * Get all rights in the system.
   *
   * @return all rights in the system
   */
  @RequestMapping(value = "/rights", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRights() {

    LOGGER.debug("Getting all rights");
    Set<Right> rights = Sets.newHashSet(rightRepository.findAll());
    Set<RightDto> rightDtos = rights.stream().map(right -> exportToDto(right)).collect(toSet());

    return ResponseEntity
        .ok(rightDtos);
  }

  /**
   * Get chosen right.
   *
   * @param rightId id of the right to get
   * @return right
   */
  @RequestMapping(value = "/rights/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getRight(@PathVariable("id") UUID rightId) {
    Right right = rightRepository.findOne(rightId);

    if (right == null) {
      return ResponseEntity
          .notFound()
          .build();
    } else {
      return ResponseEntity
          .ok(exportToDto(right));
    }
  }

  /**
   * Save a right using the provided right DTO. If the right does not exist, will create one. If it
   * does exist, will update it.
   *
   * @param rightDto provided right DTO
   * @return the saved right
   */
  @RequestMapping(value = "/rights", method = RequestMethod.PUT)
  public ResponseEntity<?> saveRight(@RequestBody RightDto rightDto) {

    Right rightToSave = Right.newRight(rightDto);

    Right storedRight = rightRepository.findFirstByName(rightToSave.getName());
    if (storedRight != null) {
      LOGGER.debug("Right found in the system, assign id");
      rightToSave.setId(storedRight.getId());
    }

    try {

      LOGGER.debug("Saving right");
      rightRepository.save(rightToSave);

    } catch (DataIntegrityViolationException dive) {

      LOGGER.error("An error occurred while saving right: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    LOGGER.debug("Saved right with id: " + rightToSave.getId());

    return ResponseEntity
        .ok()
        .body(exportToDto(rightToSave));
  }

  /**
   * Delete an existing right.
   *
   * @param rightId id of the right to delete
   * @return no content
   */
  @RequestMapping(value = "/rights/{rightId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRight(@PathVariable("rightId") UUID rightId) {

    Right storedRight = rightRepository.findOne(rightId);
    if (storedRight == null) {
      LOGGER.error("Right to delete does not exist");
      return ResponseEntity
          .notFound()
          .build();
    }

    try {

      LOGGER.debug("Deleting right");
      rightRepository.delete(rightId);

    } catch (DataIntegrityViolationException dive) {

      LOGGER.error("An error occurred while deleting right: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    return ResponseEntity
        .noContent()
        .build();
  }

  private RightDto exportToDto(Right right) {
    RightDto rightDto = new RightDto();
    right.export(rightDto);
    return rightDto;
  }
}

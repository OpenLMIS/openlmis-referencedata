package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Right;
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

import java.util.Set;
import java.util.UUID;

@Controller
public class RightController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);

  @Autowired
  private RightRepository rightRepository;

  /**
   * Get all rights in the system.
   *
   * @return all rights in the system
   */
  @RequestMapping(value = "/rights", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRights() {

    LOGGER.debug("Getting all rights");
    Iterable<Right> rights = rightRepository.findAll();

    return ResponseEntity
        .ok()
        .body(rights);
  }

  /**
   * Save a right using the provided right DTO. If the right does not exist, will create one. If it
   * does exist,
   *
   * @param rightDto provided right DTO
   * @return ResponseEntity containing the updated right
   */
  @RequestMapping(value = "/rights", method = RequestMethod.PUT)
  public ResponseEntity<?> saveRight(@RequestBody Right rightDto) {

    Right rightToSave = createRightInstance(rightDto);

    Right storedRight = rightRepository.findFirstByName(rightDto.getName());
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
        .body(rightToSave);
  }

  /**
   * Delete an existing right.
   *
   * @param rightId id of the right to delete
   * @return no content
   */
  @RequestMapping(value = "/rights/{rightId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRight(@PathVariable("rightId") UUID rightId) {

    Right persistedRight = rightRepository.findOne(rightId);
    if (persistedRight == null) {
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

  private Right createRightInstance(@RequestBody Right rightDto) {
    Right right = new Right(rightDto.getName(), rightDto.getType(),
        rightDto.getDescription());
    Set<Right> attachments = rightDto.getAttachments();
    right.attach(attachments.toArray(new Right[attachments.size()]));
    return right;
  }
}

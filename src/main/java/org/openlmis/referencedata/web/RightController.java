package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.RightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;

@Controller
public class RightController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private ExposedMessageSource messageSource;

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
   * Create a new right using the provided right DTO.
   *
   * @param rightDto right DTO with which to create the right
   * @return if successful, the new right; otherwise an HTTP error
   */
  @RequestMapping(value = "/rights", method = RequestMethod.POST)
  public ResponseEntity<?> createRight(@RequestBody Right rightDto) {

    Right newRight = createRightInstance(rightDto);

    try {

      LOGGER.debug("Saving new right");
      rightRepository.save(newRight);

    } catch (DataIntegrityViolationException dive) {
      LOGGER.error("An error occurred while saving right: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    LOGGER.debug("Saved new right with id: " + newRight.getId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(newRight);
  }

  /**
   * Update an existing right using the provided right DTO. Note, if the right does not exist, will
   * create one.
   *
   * @param rightId  id of the right to update
   * @param rightDto provided right DTO
   * @return ResponseEntity containing the updated right
   */
  @RequestMapping(value = "/rights/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRight(@PathVariable("id") UUID rightId,
                                       @RequestBody Right rightDto) {

    LOGGER.debug("Checking if right exists");
    Right persistedRight = rightRepository.findOne(rightId);

    if (persistedRight != null && !persistedRight.getName().equalsIgnoreCase(rightDto.getName())) {
      LOGGER.error("Right name does not match existing right");
      return ResponseEntity
          .badRequest()
          .body(messageSource.getMessage("referencedata.error.right-name-does-not-match-db",
              null, LocaleContextHolder.getLocale()));
    }

    Right rightToSave = createRightInstance(rightDto);
    rightToSave.setId(rightId);

    try {

      LOGGER.debug("Saving right using id: " + rightId);
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
  @RequestMapping(value = "/rights/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRight(@PathVariable("id") UUID rightId) {

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
    List<Right> attachments = rightDto.getAttachments();
    right.attach(attachments.toArray(new Right[attachments.size()]));
    return right;
  }
}

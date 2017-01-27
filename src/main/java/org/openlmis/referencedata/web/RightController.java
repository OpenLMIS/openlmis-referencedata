package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.RightMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Controller
public class RightController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);

  @Autowired
  private RightRepository rightRepository;

  public RightController(RightRepository repository) {
    this.rightRepository = Objects.requireNonNull(repository);
  }

  /**
   * Get all rights in the system.
   *
   * @return all rights in the system
   */
  @RequestMapping(value = "/rights", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRights() {
    
    rightService.checkRootAccess();

    LOGGER.debug("Getting all rights");
    Set<Right> rights = Sets.newHashSet(rightRepository.findAll());
    Set<RightDto> rightDtos = rights.stream().map(this::exportToDto).collect(toSet());

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
    
    rightService.checkRootAccess();

    Right right = rightRepository.findOne(rightId);

    if (right == null) {
      throw new NotFoundException(RightMessageKeys.ERROR_NOT_FOUND);
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

    rightService.checkRootAccess();

    if (rightDto.getAttachments() != null) {
      for (Right.Importer attachmentDto : rightDto.getAttachments()) {
        Right storedAttachment = rightRepository.findFirstByName(attachmentDto.getName());
        if (storedAttachment == null) {
          throw new ValidationMessageException(new Message(
              RightMessageKeys.ERROR_NAME_NON_EXISTENT, attachmentDto.getName()));
        }

        storedAttachment.export((RightDto) attachmentDto);
      }
    }

    Right rightToSave = Right.newRight(rightDto);

    Right storedRight = rightRepository.findFirstByName(rightToSave.getName());
    if (storedRight != null) {
      LOGGER.debug("Right found in the system, assign id");
      rightToSave.setId(storedRight.getId());
    }

    LOGGER.debug("Saving right");
    rightToSave = rightRepository.save(rightToSave);


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

    rightService.checkRootAccess();

    Right storedRight = rightRepository.findOne(rightId);
    if (storedRight == null) {
      throw new NotFoundException(RightMessageKeys.ERROR_NOT_FOUND);
    }

    LOGGER.debug("Deleting right");
    rightRepository.delete(rightId);

    return ResponseEntity
        .noContent()
        .build();
  }

  /**
   * Find a right by its name.
   *
   * @param name the name of the right to find
   * @return right
   */
  @RequestMapping(value = "/rights/search", method = RequestMethod.GET)
  public ResponseEntity<?> findRightByName(@RequestParam("name") String name) {

    rightService.checkRootAccess();

    Right foundRight = rightRepository.findFirstByName(name);
    if (foundRight == null) {
      throw new NotFoundException(RightMessageKeys.ERROR_NOT_FOUND);
    }

    LOGGER.debug("Right found, returning");
    return ResponseEntity
        .ok(Sets.newHashSet(exportToDto(foundRight)));
  }

  private RightDto exportToDto(Right right) {
    RightDto rightDto = new RightDto();
    right.export(rightDto);
    return rightDto;
  }
}

package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toSet;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.exception.AuthException;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
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

import java.util.Set;
import java.util.UUID;

@Controller
public class RoleController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  /**
   * Get all roles in the system.
   *
   * @return all roles in the system
   */
  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRoles() {

    LOGGER.debug("Getting all roles");
    Iterable<Role> roles = roleRepository.findAll();

    return ResponseEntity
        .ok()
        .body(roles);
  }

  /**
   * Create a new role using the provided role DTO.
   *
   * @param roleDto role DTO with which to create the role
   * @return if successful, the new role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles", method = RequestMethod.POST)
  public ResponseEntity<?> createRole(@RequestBody Role roleDto) {

    Role newRole;

    try {

      LOGGER.debug("Saving new role");
      newRole = createRoleInstance(roleDto);
      roleRepository.save(newRole);

    } catch (AuthException ae) {

      LOGGER.error("An error occurred while creating role object: "
          + messageSource.getMessage(ae.getMessage(), null, LocaleContextHolder.getLocale()));
      return ResponseEntity
          .badRequest()
          .body(messageSource.getMessage(ae.getMessage(), null, LocaleContextHolder.getLocale()));
    } catch (DataIntegrityViolationException dive) {

      LOGGER.error("An error occurred while saving new role: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    LOGGER.debug("Saved new role with id: " + newRole.getId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(newRole);
  }

  /**
   * Update an existing role using the provided role DTO. Note, if the role does not exist, will
   * create one.
   *
   * @param roleId  id of the role to update
   * @param roleDto provided role DTO
   * @return if successful, the updated role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRole(@PathVariable("id") UUID roleId,
                                      @RequestBody Role roleDto) {

    Role roleToSave;

    try {

      LOGGER.debug("Saving role using id: " + roleId);
      roleToSave = createRoleInstance(roleDto);
      roleToSave.setId(roleId);
      roleRepository.save(roleToSave);

    } catch (AuthException ae) {

      LOGGER.error("An error occurred while creating role object: "
          + messageSource.getMessage(ae.getMessage(), null, LocaleContextHolder.getLocale()));
      return ResponseEntity
          .badRequest()
          .body(messageSource.getMessage(ae.getMessage(), null, LocaleContextHolder.getLocale()));
    } catch (DataIntegrityViolationException dive) {

      LOGGER.error("An error occurred while saving role: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    LOGGER.debug("Saved role with id: " + roleToSave.getId());

    return ResponseEntity
        .ok()
        .body(roleToSave);
  }

  /**
   * Delete an existing role.
   *
   * @param roleId id of the role to delete
   * @return no content
   */
  @RequestMapping(value = "/roles/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRole(@PathVariable("id") UUID roleId) {

    Role storedRole = roleRepository.findOne(roleId);
    if (storedRole == null) {
      LOGGER.error("Role to delete does not exist");
      return ResponseEntity
          .notFound()
          .build();
    }

    try {

      LOGGER.debug("Deleting role");
      roleRepository.delete(roleId);

    } catch (DataIntegrityViolationException dive) {

      LOGGER.error("An error occurred while deleting role: " + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    }

    return ResponseEntity
        .noContent()
        .build();
  }

  private Role createRoleInstance(Role roleDto) throws RightTypeException, RoleException {
    if (roleDto.getRights().size() == 0) {
      throw new RoleException("referencedata.error.role-must-have-a-right");
    }

    Set<Right> rights = roleDto.getRights().stream().map(rightDto -> rightRepository
        .findFirstByName(rightDto.getName())).collect(toSet());

    return new Role(roleDto.getName(),
        roleDto.getDescription(),
        rights.toArray(new Right[rights.size()]));
  }
}

package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.util.messagekeys.RoleMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Controller
public class RoleController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  /**
   * Constructor for controller unit testing.
   *
   * @param repository      role repository
   * @param rightRepository right repository
   */
  public RoleController(RoleRepository repository, RightRepository rightRepository) {
    this.roleRepository = Objects.requireNonNull(repository);
    this.rightRepository = Objects.requireNonNull(rightRepository);
  }

  /**
   * Get all roles in the system.
   *
   * @return all roles in the system
   */
  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRoles() {

    LOGGER.debug("Getting all roles");
    Set<Role> roles = Sets.newHashSet(roleRepository.findAll());
    Set<RoleDto> roleDtos = roles.stream().map(this::exportToDto).collect(toSet());

    return ResponseEntity
        .ok()
        .body(roleDtos);
  }

  /**
   * Get specified role in the system.
   *
   * @param roleId id of the role to get
   * @return specified role
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.GET)
  public ResponseEntity<?> getRole(@PathVariable("roleId") UUID roleId) {

    LOGGER.debug("Getting role");
    Role role = roleRepository.findOne(roleId);
    if (role == null) {
      LOGGER.error("Role to get does not exist");
      return ResponseEntity
          .notFound()
          .build();
    }

    return ResponseEntity
        .ok()
        .body(exportToDto(role));
  }

  /**
   * Create a new role using the provided role DTO.
   *
   * @param roleDto role DTO with which to create the role
   * @return if successful, the new role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles", method = RequestMethod.POST)
  public ResponseEntity<?> createRole(@RequestBody RoleDto roleDto) {

    Role storedRole = roleRepository.findFirstByName(roleDto.getName());
    if (storedRole != null) {
      LOGGER.error("Role to create already exists");
      throw new DataIntegrityViolationException(RoleMessageKeys.ERROR_DUPLICATED);
    }

    LOGGER.debug("Saving new role");

    populateRights(roleDto);
    Role newRole = Role.newRole(roleDto);
    roleRepository.save(newRole);

    LOGGER.debug("Saved new role with id: " + newRole.getId());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(exportToDto(newRole));
  }

  /**
   * Update an existing role using the provided role DTO. Note, if the role does not exist, will
   * create one.
   *
   * @param roleId  id of the role to update
   * @param roleDto provided role DTO
   * @return if successful, the updated role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRole(@PathVariable("roleId") UUID roleId,
                                      @RequestBody RoleDto roleDto) {

    Role roleToSave;

    LOGGER.debug("Saving role using id: " + roleId);

    populateRights(roleDto);
    roleToSave = Role.newRole(roleDto);
    roleToSave.setId(roleId);
    roleRepository.save(roleToSave);

    LOGGER.debug("Saved role with id: " + roleToSave.getId());

    return ResponseEntity
        .ok()
        .body(exportToDto(roleToSave));
  }

  /**
   * Delete an existing role.
   *
   * @param roleId id of the role to delete
   * @return no content
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRole(@PathVariable("roleId") UUID roleId) {

    Role storedRole = roleRepository.findOne(roleId);
    if (storedRole == null) {
      LOGGER.error("Role to delete does not exist");
      return ResponseEntity
          .notFound()
          .build();
    }


    LOGGER.debug("Deleting role");
    roleRepository.delete(roleId);


    return ResponseEntity
        .noContent()
        .build();
  }

  private RoleDto exportToDto(Role role) {
    RoleDto roleDto = new RoleDto();
    role.export(roleDto);
    return roleDto;
  }

  private void populateRights(RoleDto roleDto) {
    Set<Right.Importer> rightDtos = roleDto.getRights();
    for (Right.Importer rightDto : rightDtos) {
      Right storedRight = rightRepository.findFirstByName(rightDto.getName());
      storedRight.export((RightDto) rightDto);
    }
  }
}

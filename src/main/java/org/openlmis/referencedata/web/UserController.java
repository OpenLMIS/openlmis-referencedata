package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.exception.AuthException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
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

import java.util.Set;
import java.util.UUID;

@Controller
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  ProgramRepository programRepository;

  @Autowired
  SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  FacilityRepository facilityRepository;

  /**
   * Get all roles associated with the specified user.
   *
   * @return all roles associated with the specified user
   */
  @RequestMapping(value = "/users/{userId}/roles", method = RequestMethod.GET)
  public ResponseEntity<?> getAllUserRoles(@PathVariable("userId") UUID userId) {

    LOGGER.debug("Getting all roles associated with userId: " + userId);
    User user = userRepository.findOne(userId);
    Set<RoleAssignment> roleAssignments = user.getRoleAssignments();

    return ResponseEntity
        .ok()
        .body(roleAssignments);
  }

  /**
   * Assign roles to a user using the provided role assignment DTOs.
   *
   * @param roleAssignmentDtos role assignment DTOs to associate to the user
   * @return if successful, the updated user; otherwise an HTTP error
   */
  @RequestMapping(value = "/users/{userId}/roles", method = RequestMethod.POST)
  public ResponseEntity<?> saveUserRoles(@PathVariable("userId") UUID userId,
                                         @RequestBody Set<RoleAssignmentDto> roleAssignmentDtos) {

    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    try {

      LOGGER.debug("Assigning roles to user and saving");
      for (RoleAssignmentDto roleAssignmentDto : roleAssignmentDtos) {
        RoleAssignment roleAssignment;

        UUID roleId = roleAssignmentDto.getRoleId();
        if (roleId == null) {
          LOGGER.error("Role ID is required");
          return ResponseEntity
              .badRequest()
              .body("Role ID is required");
        }
        Role role = roleRepository.findOne(roleId);

        UUID programId = roleAssignmentDto.getProgramId();
        UUID warehouseId = roleAssignmentDto.getWarehouseId();
        if (programId != null) {

          Program program = programRepository.findOne(programId);
          UUID supervisoryNodeId = roleAssignmentDto.getSupervisoryNodeId();
          if (supervisoryNodeId != null) {

            SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
            roleAssignment = new SupervisionRoleAssignment(role, program, supervisoryNode);

          } else {
            roleAssignment = new SupervisionRoleAssignment(role, program);
          }

        } else if (warehouseId != null) {

          Facility warehouse = facilityRepository.findOne(warehouseId);
          roleAssignment = new FulfillmentRoleAssignment(role, warehouse);

        } else {
          roleAssignment = new DirectRoleAssignment(role);
        }

        user.assignRoles(roleAssignment);
      }

      userRepository.save(user);

    } catch (DataIntegrityViolationException dive) {
      LOGGER.error("An error occurred while saving roles to user: "
          + dive.getRootCause().getMessage());
      return ResponseEntity
          .badRequest()
          .body(dive.getRootCause().getMessage());
    } catch (AuthException ae) {
      LOGGER.error("An error occurred while creating role assignment object: "
          + ae.getMessage());
      return ResponseEntity
          .badRequest()
          .body(ae.getMessage());
    }

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(user.getRoleAssignments());
  }

}

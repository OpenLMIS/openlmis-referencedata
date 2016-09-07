package org.openlmis.referencedata.web;

import com.fasterxml.jackson.annotation.JsonView;

import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.exception.AuthException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.View;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
@Controller
public class UserController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RightController.class);
  private static final String USER_ID = "userId";

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

  @Autowired
  RightRepository rightRepository;

  /**
   * Get all roles associated with the specified user.
   *
   * @return all roles associated with the specified user
   */
  @JsonView(View.BasicInformation.class)
  @RequestMapping(value = "/users/{userId}/roles", method = RequestMethod.GET)
  public ResponseEntity<?> getAllUserRoles(@PathVariable(USER_ID) UUID userId) {

    LOGGER.debug("Getting all roles associated with userId: " + userId);
    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

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
  @JsonView(View.BasicInformation.class)
  @RequestMapping(value = "/users/{userId}/roles", method = RequestMethod.POST)
  public ResponseEntity<?> saveUserRoles(@PathVariable(USER_ID) UUID userId,
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

        String programCode = roleAssignmentDto.getProgramCode();
        String warehouseCode = roleAssignmentDto.getWarehouseCode();
        if (programCode != null) {

          Program program = programRepository.findByCode(programCode);
          UUID supervisoryNodeId = roleAssignmentDto.getSupervisoryNodeId();
          if (supervisoryNodeId != null) {

            SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
            roleAssignment = new SupervisionRoleAssignment(role, program, supervisoryNode);

          } else {
            roleAssignment = new SupervisionRoleAssignment(role, program);
          }

        } else if (warehouseCode != null) {

          Facility warehouse = facilityRepository.findFirstByCode(warehouseCode);
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

  /**
   * Check if user has a right with certain criteria.
   *
   * @param userId            id of user to check for right
   * @param rightName         right to check
   * @param programCode       program to check
   * @param supervisoryNodeId supervisory node to check
   * @param warehouseCode     warehouse to check
   * @return if successful, true or false depending on if user has the right
   */
  @RequestMapping(value = "/users/{userId}/hasRight", method = RequestMethod.GET)
  public ResponseEntity<?> checkIfUserHasRight(@PathVariable(USER_ID) UUID userId,
                                               @RequestParam(value = "rightName") String rightName,
                                               @RequestParam(value = "programCode",
                                                   required = false) String programCode,
                                               @RequestParam(value = "supervisoryNodeId",
                                                   required = false) UUID supervisoryNodeId,
                                               @RequestParam(value = "warehouseCode",
                                                   required = false) String warehouseCode) {

    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    RightQuery rightQuery;
    Right right = rightRepository.findFirstByName(rightName);
    if (programCode != null) {

      Program program = programRepository.findByCode(programCode);
      if (supervisoryNodeId != null) {

        SupervisoryNode supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
        rightQuery = new RightQuery(right, program, supervisoryNode);

      } else {
        rightQuery = new RightQuery(right, program);
      }
    } else if (warehouseCode != null) {

      Facility warehouse = facilityRepository.findFirstByCode(warehouseCode);
      rightQuery = new RightQuery(right, warehouse);

    } else {
      rightQuery = new RightQuery(right);
    }

    boolean hasRight = user.hasRight(rightQuery);

    return ResponseEntity
        .ok()
        .body(hasRight);
  }

  /**
   * Get the programs at a user's home facility or programs that the user supervises.
   *
   * @param userId          id of user to get programs
   * @param forHomeFacility true to get home facility programs, false to get supervised programs;
   *                        default value is true
   * @return set of programs
   */
  @RequestMapping(value = "/users/{userId}/programs", method = RequestMethod.GET)
  public ResponseEntity<?> getUserPrograms(@PathVariable(USER_ID) UUID userId,
                                           @RequestParam(value = "forHomeFacility",
                                               required = false, defaultValue = "true")
                                               boolean forHomeFacility) {

    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    Set<Program> programs = forHomeFacility
        ? user.getHomeFacilityPrograms() : user.getSupervisedPrograms();

    return ResponseEntity
        .ok()
        .body(programs);
  }

  /**
   * Get all the facilities that the user supervises.
   *
   * @param userId id of user to get supervised facilities
   * @return set of supervised facilities
   */
  @RequestMapping(value = "/users/{userId}/supervisedFacilities", method = RequestMethod.GET)
  public ResponseEntity<?> getUserSupervisedFacilities(@PathVariable(USER_ID) UUID userId) {

    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    Set<Facility> supervisedFacilities = user.getSupervisedFacilities();

    return ResponseEntity
        .ok()
        .body(supervisedFacilities);
  }
}
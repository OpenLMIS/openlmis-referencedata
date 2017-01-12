package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Code;
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
import org.openlmis.referencedata.dto.DetailedRoleAssignmentDto;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.ExternalApiException;
import org.openlmis.referencedata.exception.RoleAssignmentException;
import org.openlmis.referencedata.exception.UnknownIdException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.util.ErrorResponse;
import org.openlmis.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;

@NoArgsConstructor
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
@Controller
public class UserController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
  private static final String USER_ID = "userId";

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private Validator validator;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  /**
   * Constructor for controller unit testing.
   */
  public UserController(UserService userService,
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        RightRepository rightRepository,
                        ProgramRepository programRepository,
                        SupervisoryNodeRepository supervisoryNodeRepository,
                        FacilityRepository facilityRepository ) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.rightRepository = rightRepository;
    this.programRepository = programRepository;
    this.supervisoryNodeRepository = supervisoryNodeRepository;
    this.facilityRepository = facilityRepository;
  }

  /**
   * Custom endpoint for creating and updating users and their roles.
   */
  @RequestMapping(value = "/users", method = RequestMethod.PUT)
  public ResponseEntity<?> saveUser(@RequestBody @Valid UserDto userDto,
                                    BindingResult bindingResult,
                                    OAuth2Authentication auth) {
    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    String token = details.getTokenValue();

    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }

    String homeFacilityCode = userDto.fetchHomeFacilityCode();
    if (homeFacilityCode != null) {
      Facility homeFacility = facilityRepository.findFirstByCode(userDto.fetchHomeFacilityCode());
      if (homeFacility == null) {
        LOGGER.error("Home facility does not exist");
        return ResponseEntity
            .badRequest()
            .body("Home facility does not exist");
      } else {
        userDto.setHomeFacility(homeFacility);
      }
    }

    try {

      User userToSave = User.newUser(userDto);

      Set<RoleAssignmentDto> roleAssignmentDtos = userDto.getRoleAssignments();
      if (roleAssignmentDtos != null) {

        boolean foundNullRoleId = roleAssignmentDtos.stream().anyMatch(
            roleAssignmentDto -> roleAssignmentDto.getRoleId() == null);
        if (foundNullRoleId) {
          return ResponseEntity
              .badRequest()
              .body("Role ID is required");
        }

        assignRolesToUser(roleAssignmentDtos, userToSave);
      }

      userService.save(userToSave, token);

      return ResponseEntity
          .ok(exportUserToDto(userToSave));

    } catch (ExternalApiException ex) {

      ErrorResponse errorResponse =
          new ErrorResponse("An error occurred while saving user", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse);
    }
  }

  /**
   * Get all users and their roles.
   *
   * @return Users.
   */
  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public ResponseEntity<?> getAllUsers() {

    LOGGER.debug("Getting all users");
    Set<User> users = Sets.newHashSet(userRepository.findAll());
    Set<UserDto> userDtos = users.stream().map(user -> exportUserToDto(user)).collect(toSet());

    return ResponseEntity
        .ok(userDtos);
  }

  /**
   * Get chosen user and role.
   *
   * @param userId UUID of user whose we want to get
   * @return User.
   */
  @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
  public ResponseEntity<?> getUser(@PathVariable("userId") UUID userId) {

    LOGGER.debug("Getting user");
    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User to get does not exist");
      return ResponseEntity
          .notFound()
          .build();
    } else {
      return ResponseEntity
          .ok(exportUserToDto(user));
    }
  }

  /**
   * Get all rights and roles of the specified user.
   *
   * @param userId UUID of the user to retrieve
   * @return a set of user role assignments
   */
  @RequestMapping(value = "/users/{userId}/roleAssignments", method = RequestMethod.GET)
  public ResponseEntity<?> getUserRightsAndRoles(@PathVariable("userId") UUID userId) {

    User user = userRepository.findOne(userId);
    if (user == null) {
      ErrorResponse errorResponse = new ErrorResponse(
          "referencedata.error.user.not-found", "User with ID " + userId + " was not found.");
      return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    } else {
      Set<RoleAssignment> roleAssignments = user.getRoleAssignments();
      return ResponseEntity.ok(exportRoleAssignmentsToDtos(roleAssignments));
    }
  }

  /**
   * Allows deleting user.
   *
   * @param userId UUID of user whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteUser(@PathVariable("userId") UUID userId) {
    User user = userRepository.findOne(userId);
    if (user == null) {
      return ResponseEntity
          .notFound()
          .build();
    } else {
      userRepository.delete(userId);
      return ResponseEntity
          .noContent()
          .build();
    }
  }

  /**
   * Returns all users with matched parameters
   *
   * @param queryMap request parameters (username, firstName, lastName, homeFacility, active,
   *                 verified, loginRestricted) and JSON extraData.
   * @return ResponseEntity with list of all Users matching provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/users/search", method = RequestMethod.POST)
  public ResponseEntity<?> searchUsers(
      @RequestBody Map<String, Object> queryMap) {
    List<User> result = userService.searchUsers(queryMap);

    return ResponseEntity
        .ok(exportUsersToDtos(result));
  }

  /**
   * Check if user has a right with certain criteria.
   *
   * @param userId      id of user to check for right
   * @param rightId     right to check
   * @param programId   program to check (for supervision rights)
   * @param facilityId  facility to check (for supervision rights)
   * @param warehouseId warehouse to check (for order fulfillment rights)
   * @return if successful, true or false depending on if user has the right
   */
  @RequestMapping(value = "/users/{userId}/hasRight", method = RequestMethod.GET)
  public ResponseEntity<?> checkIfUserHasRight(@PathVariable(USER_ID) UUID userId,
                                               @RequestParam(value = "rightId") UUID rightId,
                                               @RequestParam(value = "programId",
                                                   required = false) UUID programId,
                                               @RequestParam(value = "facilityId",
                                                   required = false) UUID facilityId,
                                               @RequestParam(value = "warehouseId",
                                                   required = false) UUID warehouseId) {

    User user = validateUser(userId);

    RightQuery rightQuery;
    Right right = rightRepository.findOne(rightId);
    if (programId != null) {

      Program program = programRepository.findOne(programId);
      if (facilityId != null) {

        Facility facility = facilityRepository.findOne(facilityId);
        rightQuery = new RightQuery(right, program, facility);

      } else {
        return ResponseEntity
            .badRequest()
            .body("If program code is specified, facility code must also be specified");
      }
    } else if (warehouseId != null) {

      Facility warehouse = facilityRepository.findOne(warehouseId);
      rightQuery = new RightQuery(right, warehouse);

    } else {
      rightQuery = new RightQuery(right);
    }

    boolean hasRight = user.hasRight(rightQuery);

    return ResponseEntity
        .ok()
        .body(new ResultDto<>(hasRight));
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
  public ResponseEntity<Set<Program>> getUserPrograms(@PathVariable(USER_ID) UUID userId,
                                                      @RequestParam(value = "forHomeFacility",
                                                          required = false,
                                                          defaultValue = "true")
                                                            boolean forHomeFacility) {
    User user = validateUser(userId);
    Set<Program> programs = forHomeFacility
        ? user.getHomeFacilityPrograms() : user.getSupervisedPrograms();

    return ResponseEntity
        .ok()
        .body(programs);
  }

  /**
   * Get all the facilities that the user supervises, by right and program.
   *
   * @param userId    id of user to get supervised facilities
   * @param rightId   right to check
   * @param programId program to check
   * @return set of supervised facilities
   */
  @RequestMapping(value = "/users/{userId}/supervisedFacilities", method = RequestMethod.GET)
  public ResponseEntity<Set<Facility>> getUserSupervisedFacilities(
      @PathVariable(USER_ID) UUID userId,
      @RequestParam(value = "rightId") UUID rightId,
      @RequestParam(value = "programId") UUID programId) {

    User user = (User) validateId(userId, userRepository).orElseThrow( () ->
        new UnknownIdException(entityNotFoundMessage("user", userId)));

    Right right = (Right) validateId(rightId, rightRepository).orElseThrow( () ->
        new ValidationMessageException(entityNotFoundMessage("right", rightId )));

    Program program = (Program) validateId(programId, programRepository).orElseThrow( () ->
        new ValidationMessageException(entityNotFoundMessage("program", programId)));

    Set<Facility> supervisedFacilities = user.getSupervisedFacilities(right, program);
    return ResponseEntity
        .ok()
        .body(supervisedFacilities);
  }

  /**
   * Get all the facilities that the user has fulfillment rights for.
   *
   * @param userId id of user to get fulfillment facilities
   * @return set of fulfillment facilities
   */
  @RequestMapping(value = "/users/{userId}/fulfillmentFacilities", method = RequestMethod.GET)
  public ResponseEntity<Set<Facility>> getUserFulfillmentFacilities(
      @PathVariable(USER_ID) UUID userId) {

    User user = validateUser(userId);
    Set<Facility> facilities = user.getFulfillmentFacilities();

    return ResponseEntity
        .ok()
        .body(facilities);
  }

  /**
   * Resets a user's password.
   */
  @RequestMapping(value = "/users/passwordReset", method = RequestMethod.POST)
  public ResponseEntity<?> passwordReset(
      @RequestBody @Valid PasswordResetRequest passwordResetRequest,
      BindingResult bindingResult, OAuth2Authentication auth) {

    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    String token = details.getTokenValue();

    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }

    try {
      userService.passwordReset(passwordResetRequest, token);

      return new ResponseEntity<>(HttpStatus.OK);
    } catch (ExternalApiException ex) {
      ErrorResponse errorResponse =
          new ErrorResponse("Could not reset user password", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  @RequestMapping(value = "/users/changePassword", method = RequestMethod.POST)
  public ResponseEntity<?> changePassword(
      @RequestBody @Valid PasswordChangeRequest passwordChangeRequest, BindingResult bindingResult,
      OAuth2Authentication auth) {
    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    String token = details.getTokenValue();

    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }

    try {
      userService.changePassword(passwordChangeRequest, token);

      return new ResponseEntity(HttpStatus.OK);
    } catch (ExternalApiException ex) {
      ErrorResponse errorResponse =
          new ErrorResponse("Could not change user password", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private User validateUser(UUID userId) {
    User user = userRepository.findOne(userId);
    if (user == null) {
      String messageCode = "referencedata.error.id.not-found";
      throw new UnknownIdException( new Message(messageCode, userId) );
    }

    return user;
  }

  // finds a given entity by id, wrapping any null in an Optional
  private Optional<BaseEntity> validateId(
      UUID id,
      PagingAndSortingRepository<? extends BaseEntity, UUID> repository) {

    BaseEntity entity = repository.findOne(id);
    return (null != entity) ? Optional.of(entity) : Optional.empty();
  }

  // helper to construct not found message for entities
  private Message entityNotFoundMessage(String entityType, UUID id) {
    return new Message("referencedata.error." + entityType + ".not-found",
        id);
  }

  private void assignRolesToUser(Set<RoleAssignmentDto> roleAssignmentDtos, User user)
      throws RoleAssignmentException {
    LOGGER.debug("Assigning roles to user and saving");
    for (RoleAssignmentDto roleAssignmentDto : roleAssignmentDtos) {
      RoleAssignment roleAssignment;

      Role role = roleRepository.findOne(roleAssignmentDto.getRoleId());

      if (role.getRights().isEmpty()) {
        throw new ValidationMessageException( new Message(
            "referencedata.error.assigned-role-must-have-a-right",
            role.getName() ));
      }

      String programCode = roleAssignmentDto.getProgramCode();
      String warehouseCode = roleAssignmentDto.getWarehouseCode();
      if (programCode != null) {

        Program program = programRepository.findByCode(Code.code(programCode));
        String supervisoryNodeCode = roleAssignmentDto.getSupervisoryNodeCode();
        if (supervisoryNodeCode != null) {

          SupervisoryNode supervisoryNode = supervisoryNodeRepository.findByCode(
              supervisoryNodeCode);
          roleAssignment = new SupervisionRoleAssignment(role, user, program, supervisoryNode);

        } else {
          roleAssignment = new SupervisionRoleAssignment(role, user, program);
        }

      } else if (warehouseCode != null) {

        Facility warehouse = facilityRepository.findFirstByCode(warehouseCode);
        roleAssignment = new FulfillmentRoleAssignment(role, user, warehouse);

      } else {
        roleAssignment = new DirectRoleAssignment(role, user);
      }

      user.assignRoles(roleAssignment);
    }
  }

  private Set<DetailedRoleAssignmentDto> exportRoleAssignmentsToDtos(
      Set<RoleAssignment> roleAssignments) {

    Set<DetailedRoleAssignmentDto> assignmentDtos = new HashSet<>();

    for (RoleAssignment assignment : roleAssignments) {
      DetailedRoleAssignmentDto assignmentDto = new DetailedRoleAssignmentDto();

      if (assignment instanceof SupervisionRoleAssignment) {
        ((SupervisionRoleAssignment) assignment).export(assignmentDto);
      } else if (assignment instanceof FulfillmentRoleAssignment) {
        ((FulfillmentRoleAssignment) assignment).export(assignmentDto);
      } else {
        ((DirectRoleAssignment) assignment).export(assignmentDto);
      }

      assignmentDtos.add(assignmentDto);
    }

    return assignmentDtos;
  }

  private UserDto exportUserToDto(User user) {
    UserDto userDto = new UserDto();
    user.export(userDto);
    return userDto;
  }

  private List<UserDto> exportUsersToDtos(List<User> users) {
    return users.stream().map(this::exportUserToDto).collect(toList());
  }
}

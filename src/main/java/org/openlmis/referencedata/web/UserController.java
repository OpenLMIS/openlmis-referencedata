package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

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
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.AuthException;
import org.openlmis.referencedata.exception.ExternalApiException;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleAssignmentException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.util.ErrorResponse;
import org.openlmis.referencedata.util.PasswordChangeRequest;
import org.openlmis.referencedata.util.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;
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
  private ExposedMessageSource messageSource;

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
                        FacilityRepository facilityRepository,
                        ExposedMessageSource messageSource) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.rightRepository = rightRepository;
    this.programRepository = programRepository;
    this.supervisoryNodeRepository = supervisoryNodeRepository;
    this.facilityRepository = facilityRepository;
    this.messageSource = messageSource;
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
          .ok(exportToDto(userToSave));

    } catch (ExternalApiException ex) {

      ErrorResponse errorResponse =
          new ErrorResponse("An error occurred while saving user", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse);
    } catch (AuthException ae) {
      LOGGER.error("An error occurred while creating role assignment object: "
          + ae.getMessage());
      return ResponseEntity
          .badRequest()
          .body(ae.getMessage());
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
    Set<UserDto> userDtos = users.stream().map(user -> exportToDto(user)).collect(toSet());

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
          .ok(exportToDto(user));
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
   * @param username     username of user we want to search.
   * @param firstName    firstName of user we want to search.
   * @param lastName     lastName of user we want to search.
   * @param homeFacility homeFacility of user we want to search.
   * @param active       is the user account active.
   * @param verified     is the user account verified.
   * @return ResponseEntity with list of all Users matching provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/users/search", method = RequestMethod.GET)
  public ResponseEntity<?> searchUsers(
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "firstName", required = false) String firstName,
      @RequestParam(value = "lastName", required = false) String lastName,
      @RequestParam(value = "homeFacility", required = false) Facility homeFacility,
      @RequestParam(value = "active", required = false) Boolean active,
      @RequestParam(value = "verified", required = false) Boolean verified) {
    List<User> result = userService.searchUsers(username, firstName,
        lastName, homeFacility, active, verified);

    return ResponseEntity
        .ok(exportToDtos(result));
  }

  /**
   * Check if user has a right with certain criteria.
   *
   * @param userId              id of user to check for right
   * @param rightName           right to check
   * @param programCode         program to check
   * @param supervisoryNodeCode supervisory node to check
   * @param warehouseCode       warehouse to check
   * @return if successful, true or false depending on if user has the right
   */
  @RequestMapping(value = "/users/{userId}/hasRight", method = RequestMethod.GET)
  public ResponseEntity<?> checkIfUserHasRight(@PathVariable(USER_ID) UUID userId,
                                               @RequestParam(value = "rightName") String rightName,
                                               @RequestParam(value = "programCode",
                                                   required = false) String programCode,
                                               @RequestParam(value = "supervisoryNodeCode",
                                                   required = false) String supervisoryNodeCode,
                                               @RequestParam(value = "warehouseCode",
                                                   required = false) String warehouseCode) {

    User user;
    try {
      user = validateUser(userId);
    } catch (AuthException err) {
      return ResponseEntity
          .notFound()
          .build();
    }

    RightQuery rightQuery;
    Right right = rightRepository.findFirstByName(rightName);
    if (programCode != null) {

      Program program = programRepository.findByCode(Code.code(programCode));
      if (supervisoryNodeCode != null) {

        SupervisoryNode supervisoryNode = supervisoryNodeRepository.findByCode(supervisoryNodeCode);
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
    try {
      User user = validateUser(userId);
      Set<Program> programs = forHomeFacility
          ? user.getHomeFacilityPrograms() : user.getSupervisedPrograms();

      return ResponseEntity
          .ok()
          .body(programs);
    } catch (AuthException err) {
      return ResponseEntity
          .notFound()
          .build();
    }
  }

  /**
   * Get all the facilities that the user supervises.
   *
   * @param userId id of user to get supervised facilities
   * @return set of supervised facilities
   */
  @RequestMapping(value = "/users/{userId}/supervisedFacilities", method = RequestMethod.GET)
  public ResponseEntity<?> getUserSupervisedFacilities(@PathVariable(USER_ID) UUID userId) {
    try {
      User user = validateUser(userId);
      Set<Facility> supervisedFacilities = user.getSupervisedFacilities();

      return ResponseEntity
          .ok()
          .body(supervisedFacilities);
    } catch (AuthException err) {
      return ResponseEntity
          .notFound()
          .build();
    }
  }

  /**
   * Get all the facilities that the user has fulfillment rights for.
   *
   * @param userId id of user to get fulfillment facilities
   * @return set of fulfillment facilities
   */
  @RequestMapping(value = "/users/{userId}/fulfillmentFacilities", method = RequestMethod.GET)
  public ResponseEntity<?> getUserFulfillmentFacilities(@PathVariable(USER_ID) UUID userId) {
    try {
      User user = validateUser(userId);
      Set<Facility> facilities = user.getFulfillmentFacilities();

      return ResponseEntity
          .ok()
          .body(facilities);
    } catch (AuthException err) {
      return ResponseEntity
          .notFound()
          .build();
    }
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

  private User validateUser(UUID userId) throws AuthException {
    User user = userRepository.findOne(userId);
    if (user == null) {
      String messageCode = "referencedata.error.id.not-found";
      Object[] args = { userId };

      LOGGER.error(messageSource.getMessage(messageCode, args, Locale.ENGLISH));
      throw new AuthException(
          messageSource.getMessage(messageCode, args, LocaleContextHolder.getLocale()));
    }

    return user;
  }

  private void assignRolesToUser(Set<RoleAssignmentDto> roleAssignmentDtos, User user)
      throws RightTypeException, RoleAssignmentException {
    LOGGER.debug("Assigning roles to user and saving");
    for (RoleAssignmentDto roleAssignmentDto : roleAssignmentDtos) {
      RoleAssignment roleAssignment;

      Role role = roleRepository.findOne(roleAssignmentDto.getRoleId());

      String programCode = roleAssignmentDto.getProgramCode();
      String warehouseCode = roleAssignmentDto.getWarehouseCode();
      if (programCode != null) {

        Program program = programRepository.findByCode(Code.code(programCode));
        String supervisoryNodeCode = roleAssignmentDto.getSupervisoryNodeCode();
        if (supervisoryNodeCode != null) {

          SupervisoryNode supervisoryNode = supervisoryNodeRepository.findByCode(
              supervisoryNodeCode);
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
  }

  private UserDto exportToDto(User user) {
    UserDto userDto = new UserDto();
    user.export(userDto);
    return userDto;
  }

  private List<UserDto> exportToDtos(List<User> users) {
    return users.stream().map(this::exportToDto).collect(toList());
  }
}

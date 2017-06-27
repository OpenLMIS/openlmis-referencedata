/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

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
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.DetailedRoleAssignmentDto;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.dto.ResultDto;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.RightMessageKeys;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.openlmis.referencedata.validate.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;

@NoArgsConstructor
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
@Controller
@Transactional
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

  @Autowired
  private UserValidator userValidator;

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
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserDto saveUser(@RequestBody @Valid UserDto userDto,
                          BindingResult bindingResult) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    userValidator.validate(userDto, bindingResult);
    if (bindingResult.hasErrors()) {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }

    String homeFacilityCode = userDto.fetchHomeFacilityCode();
    if (homeFacilityCode != null) {
      Facility homeFacility = facilityRepository.findFirstByCode(userDto.fetchHomeFacilityCode());
      if (homeFacility == null) {
        LOGGER.error("Home facility does not exist");
        throw new ValidationMessageException(UserMessageKeys.ERROR_HOME_FACILITY_NON_EXISTENT);
      } else {
        userDto.setHomeFacility(homeFacility);
      }
    }
    User userToSave = User.newUser(userDto);

    Set<RoleAssignmentDto> roleAssignmentDtos = userDto.getRoleAssignments();
    if (roleAssignmentDtos != null) {

      boolean foundNullRoleId = roleAssignmentDtos.stream().anyMatch(
          roleAssignmentDto -> roleAssignmentDto.getRoleId() == null);
      if (foundNullRoleId) {
        throw new ValidationMessageException(UserMessageKeys.ERROR_ROLE_ID_NULL);
      }

      assignRolesToUser(roleAssignmentDtos, userToSave);
    }
    userRepository.save(userToSave);

    return exportUserToDto(userToSave);
  }

  /**
   * Get all users and their roles.
   *
   * @return the Users.
   */
  @RequestMapping(value = "/users", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<UserDto> getAllUsers() {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    LOGGER.debug("Getting all users");
    Set<User> users = Sets.newHashSet(userRepository.findAll());
    return users.stream().map(this::exportUserToDto).collect(toSet());
  }

  /**
   * Get chosen user and role.
   *
   * @param userId UUID of user whose we want to get.
   * @return the User.
   */
  @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserDto getUser(@PathVariable("userId") UUID userId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    LOGGER.debug("Getting user");
    User user = userRepository.findOne(userId);
    if (user == null) {
      LOGGER.error("User to get does not exist");
      throw new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportUserToDto(user);
    }
  }

  /**
   * Get all rights and roles of the specified user.
   *
   * @param userId UUID of the user to retrieve.
   * @return a set of user role assignments.
   */
  @RequestMapping(value = "/users/{userId}/roleAssignments", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<DetailedRoleAssignmentDto> getUserRightsAndRoles(@PathVariable("userId") UUID userId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = userRepository.findOne(userId);
    if (user == null) {
      throw new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND);
    } else {
      Set<RoleAssignment> roleAssignments = user.getRoleAssignments();
      return exportRoleAssignmentsToDtos(roleAssignments);
    }
  }

  /**
   * Allows deleting user.
   *
   * @param userId UUID of user whose we want to delete
   */
  @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteUser(@PathVariable("userId") UUID userId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = userRepository.findOne(userId);
    if (user == null) {
      throw new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND);
    } else {
      userRepository.delete(userId);
    }
  }

  /**
   * Returns all users with matched parameters.
   *
   * @param queryMap request parameters (username, firstName, lastName, email, homeFacility,
   *                 active, verified, loginRestricted) and JSON extraData.
   *
   *                 For firstName, lastName, email: finds any values that have entered
   *                 string value in any position of searched field. Not case sensitive.
   *
   *                 Other fields: entered string value must equal to searched value.
   *
   * @param pageable Pageable object that allows client to optionally add "page" (page number)
   *                 and "size" (page size) query parameters to the request.
   * @return a list of all Users matching provided parameters.
   */
  @RequestMapping(value = "/users/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<UserDto> searchUsers(
      @RequestBody Map<String, Object> queryMap, Pageable pageable) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    List<User> result = userService.searchUsers(queryMap);
    List<UserDto> userDtos = exportUsersToDtos(result);

    return Pagination.getPage(userDtos, pageable);
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
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResultDto<Boolean> checkIfUserHasRight(@PathVariable(USER_ID) UUID userId,
                                               @RequestParam(value = "rightId") UUID rightId,
                                               @RequestParam(value = "programId",
                                                   required = false) UUID programId,
                                               @RequestParam(value = "facilityId",
                                                   required = false) UUID facilityId,
                                               @RequestParam(value = "warehouseId",
                                                   required = false) UUID warehouseId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = validateUser(userId);

    RightQuery rightQuery;
    Right right = rightRepository.findOne(rightId);
    if (programId != null) {

      Program program = programRepository.findOne(programId);
      if (facilityId != null) {

        Facility facility = facilityRepository.findOne(facilityId);
        rightQuery = new RightQuery(right, program, facility);

      } else {
        throw new ValidationMessageException(UserMessageKeys.ERROR_PROGRAM_WITHOUT_FACILITY);
      }
    } else if (warehouseId != null) {

      Facility warehouse = facilityRepository.findOne(warehouseId);
      rightQuery = new RightQuery(right, warehouse);

    } else {
      rightQuery = new RightQuery(right);
    }

    boolean hasRight = user.hasRight(rightQuery);

    return new ResultDto<>(hasRight);
  }

  /**
   * Get the programs at a user's home facility or programs that the user supervises.
   *
   * @param userId          id of user to get programs
   * @param forHomeFacility true to get home facility programs, false to get supervised programs;
   *                        default value is true
   * @return a set of programs
   */
  @RequestMapping(value = "/users/{userId}/programs", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<ProgramDto> getUserPrograms(@PathVariable(USER_ID) UUID userId,
                                         @RequestParam(
                                             value = "forHomeFacility",
                                             required = false,
                                             defaultValue = "true") boolean forHomeFacility
                                         ) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = validateUser(userId);
    Set<Program> programs = forHomeFacility ? user.getHomeFacilityPrograms() :
        user.getSupervisedPrograms();

    return programsToDto(programs);
  }

  /**
   * Get the programs at a user's home facility that are supported by home facility. Support must be
   * active and supported program must be active.
   *
   * @param userId id of user to get programs
   * @return a set of programs
   */
  @RequestMapping(value = "/users/{userId}/supportedPrograms", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<ProgramDto> getUserSupportedPrograms(@PathVariable(USER_ID) UUID userId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = validateUser(userId);

    Facility homeFacility = user.getHomeFacility();
    Set<UUID> supportedProgramsIds;
    if (homeFacility != null) {
      supportedProgramsIds = homeFacility.getSupportedPrograms().stream()
          .filter(SupportedProgram::getActive)
          .filter(sp -> sp.getProgram().getActive() != null
              && sp.getProgram().getActive())
          .map(sp -> sp.getProgram().getId())
          .collect(Collectors.toSet());
    } else {
      return Collections.emptySet();
    }

    Set<Program> filteredPrograms = user.getHomeFacilityPrograms().stream()
        .filter(program -> supportedProgramsIds.contains(program.getId()))
        .collect(Collectors.toSet());

    return programsToDto(filteredPrograms);
  }

  /**
   * Get all the facilities that the user supervises, by right and program.
   *
   * @param userId    id of user to get supervised facilities
   * @param rightId   right to check
   * @param programId program to check
   * @return a set of supervised facilities
   */
  @RequestMapping(value = "/users/{userId}/supervisedFacilities", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<FacilityDto> getUserSupervisedFacilities(
      @PathVariable(USER_ID) UUID userId,
      @RequestParam(value = "rightId") UUID rightId,
      @RequestParam(value = "programId") UUID programId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = (User) validateId(userId, userRepository).orElseThrow( () ->
        new NotFoundException(new Message(UserMessageKeys.ERROR_NOT_FOUND_WITH_ID, userId)));

    Right right = (Right) validateId(rightId, rightRepository).orElseThrow( () ->
        new ValidationMessageException(
            new Message(RightMessageKeys.ERROR_NOT_FOUND_WITH_ID, rightId)));

    Program program = (Program) validateId(programId, programRepository).orElseThrow( () ->
        new ValidationMessageException(
            new Message(ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, programId)));

    Set<Facility> supervisedFacilities = user.getSupervisedFacilities(right, program);
    return facilitiesToDto(supervisedFacilities);
  }

  /**
   * Get all the facilities that the user has fulfillment rights for.
   *
   * @param userId id of user to get fulfillment facilities
   * @return a set of fulfillment facilities
   */
  @RequestMapping(value = "/users/{userId}/fulfillmentFacilities", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<FacilityDto> getUserFulfillmentFacilities(
      @PathVariable(USER_ID) UUID userId,
      @RequestParam(value = "rightId") UUID rightId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    User user = validateUser(userId);
    Right right = (Right) validateId(rightId, rightRepository).orElseThrow( () ->
        new ValidationMessageException(
            new Message(RightMessageKeys.ERROR_NOT_FOUND_WITH_ID, rightId)));

    Set<Facility> facilities = user.getFulfillmentFacilities(right);

    return facilitiesToDto(facilities);
  }

  /**
   * Searches for users based having the specified right. The params are required
   * based on the type of the right.
   * @param rightId the ID of the right, always required
   * @param programId the ID of the program, required for supervision rights
   * @param supervisoryNodeId the ID of the supervisory node,
   *                          required for supervision rights
   * @param warehouseId the ID of the warehouse, required for fulfillment rights
   * @return users with the right assigned, matching the criteria
   */
  @RequestMapping(value = "/users/rightSearch", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserDto> rightSearch(@RequestParam UUID rightId,
                                   @RequestParam(required = false) UUID programId,
                                   @RequestParam(required = false) UUID supervisoryNodeId,
                                   @RequestParam(required = false) UUID warehouseId) {
    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    Set<User> users = userService.rightSearch(rightId, programId,
        supervisoryNodeId, warehouseId);

    return exportUsersToDtos(users);
  }

  /**
   * Get the audit information related to users.
   *
   * @param author  The author of the changes which should be returned. If null or empty, changes
   *               are returned regardless of author.
   * @param changedPropertyName  The name of the property about which changes should be returned.
   *                             If null or empty, changes associated with any and all properties
   *                             are returned.
   * @param page  A Pageable object that allows client to optionally add "page" (page number) and
   *              "size" (page size) query parameters to the request.
   * @return the list of all matching audit logs as string
   */
  @RequestMapping(value = "/users/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getUsersAuditLog(
      @PathVariable("id") UUID userId,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {

    rightService.checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    //Return a 404 if the specified user can't be found
    User user = userRepository.findOne(userId);
    if (user == null) {
      throw new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND);
    }

    String auditLogs = getAuditLog(User.class, userId, author, changedPropertyName, page,
        returnJson);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(returnJson ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN);

    return new ResponseEntity<>(auditLogs, headers, HttpStatus.OK);
  }

  private User validateUser(UUID userId) {
    User user = userRepository.findOne(userId);
    if (user == null) {
      throw new NotFoundException(new Message(UserMessageKeys.ERROR_NOT_FOUND_WITH_ID, userId));
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

  private void assignRolesToUser(Set<RoleAssignmentDto> roleAssignmentDtos, User user) {
    LOGGER.debug("Assigning roles to user and saving");
    for (RoleAssignmentDto roleAssignmentDto : roleAssignmentDtos) {
      RoleAssignment roleAssignment;

      Role role = roleRepository.findOne(roleAssignmentDto.getRoleId());

      if (role.getRights().isEmpty()) {
        throw new ValidationMessageException(new Message(
            UserMessageKeys.ERROR_ASSIGNED_ROLE_RIGHTS_EMPTY, role.getName()));
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

  private List<UserDto> exportUsersToDtos(Collection<User> users) {
    return users.stream().map(this::exportUserToDto).collect(toList());
  }

  private Set<FacilityDto> facilitiesToDto(Collection<Facility> facilities) {
    Set<FacilityDto> dtos = new HashSet<>();
    for (Facility facility : facilities) {
      FacilityDto dto = new FacilityDto();
      facility.export(dto);
      dtos.add(dto);
    }

    return dtos;
  }

  private Set<ProgramDto> programsToDto(Collection<Program> programs) {
    Set<ProgramDto> dtos = new HashSet<>();

    for (Program program : programs) {
      ProgramDto dto = ProgramDto.newInstance(program);
      dtos.add(dto);
    }

    return dtos;
  }
}

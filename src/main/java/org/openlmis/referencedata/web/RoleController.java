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

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.util.messagekeys.RoleMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Controller
@Transactional
public class RoleController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  /**
   * Get all roles in the system.
   *
   * @return all roles in the system.
   */
  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<RoleDto> getAllRoles() {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);

    LOGGER.debug("Getting all roles");
    Set<Role> roles = Sets.newHashSet(roleRepository.findAll());
    return roles.stream().map(this::exportToDto).collect(toSet());
  }

  /**
   * Get specified role in the system.
   *
   * @param roleId id of the role to get.
   * @return the specified role.
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RoleDto getRole(@PathVariable("roleId") UUID roleId) {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);

    LOGGER.debug("Getting role");
    Role role = roleRepository.findOne(roleId);
    if (role == null) {
      throw new NotFoundException(RoleMessageKeys.ERROR_NOT_FOUND);
    }

    return exportToDto(role);
  }

  /**
   * Get the audit information related to role.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/roles/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getRoleAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);

    //Return a 404 if the specified instance can't be found
    Role instance = roleRepository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(RoleMessageKeys.ERROR_NOT_FOUND);
    }

    String auditLogs = getAuditLog(Role.class, id, author, changedPropertyName, page, returnJson);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(returnJson ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN);

    return new ResponseEntity<>(auditLogs, headers, HttpStatus.OK);
  }

  /**
   * Create a new role using the provided role DTO.
   *
   * @param roleDto a role DTO with which to create the role.
   * @return the new role.
   */
  @RequestMapping(value = "/roles", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public RoleDto createRole(@RequestBody RoleDto roleDto) {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

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

    return exportToDto(newRole);
  }

  /**
   * Update an existing role using the provided role DTO. Note, if the role does not exist, will
   * create one.
   *
   * @param roleId  id of the role to update.
   * @param roleDto provided role DTO.
   * @return the updated role.
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RoleDto updateRole(@PathVariable("roleId") UUID roleId,
                            @RequestBody RoleDto roleDto) {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    Role roleToSave;
    LOGGER.debug("Saving role using id: " + roleId);

    populateRights(roleDto);
    roleToSave = Role.newRole(roleDto);
    roleToSave.setId(roleId);
    roleRepository.save(roleToSave);

    LOGGER.debug("Saved role with id: " + roleToSave.getId());

    return exportToDto(roleToSave);
  }

  /**
   * Delete an existing role.
   *
   * @param roleId id of the role to delete.
   */
  @RequestMapping(value = "/roles/{roleId}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRole(@PathVariable("roleId") UUID roleId) {
    rightService.checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    Role storedRole = roleRepository.findOne(roleId);
    if (storedRole == null) {
      throw new NotFoundException(RoleMessageKeys.ERROR_NOT_FOUND);
    }

    LOGGER.debug("Deleting role");
    roleRepository.delete(roleId);
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

package org.openlmis.referencedata.web;

import static java.util.stream.Collectors.toList;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class RoleController {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  /**
   * Create a new role using the provided role object.
   *
   * @return if successful, the new role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles", method = RequestMethod.POST)
  public ResponseEntity<?> createRole(@RequestBody Role roleDto) {
    try {

      Role role = createRoleInstance(roleDto);

      roleRepository.save(role);

      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(role);

    } catch (RightTypeException rte) {
      return ResponseEntity
          .badRequest()
          .body(messageSource.getMessage(rte.getMessage(), null, LocaleContextHolder.getLocale()));
    }
  }

  /**
   * Update an existing role using the provided role object. Note, if the role does not
   *
   * @return if successful, the updated role; otherwise an HTTP error
   */
  @RequestMapping(value = "/roles/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRole(@PathVariable("id") String id,
                                      @RequestBody Role roleDto) {
    try {

      Role role = roleRepository.findFirstByName(roleDto.getName());

      if (role == null) {
        role = createRoleInstance(roleDto);
        role.setId(UUID.fromString(id));
      }
      
      if (!role.getName().equalsIgnoreCase(roleDto.getName())) {
        return ResponseEntity
            .badRequest()
            .body(messageSource.getMessage("referencedata.message.role-name-does-not-match-db",
                null, LocaleContextHolder.getLocale()));
      }

      roleRepository.save(role);

      return ResponseEntity
          .ok()
          .body(role);

    } catch (RightTypeException rte) {
      return ResponseEntity
          .badRequest()
          .body(messageSource.getMessage(rte.getMessage(), null, LocaleContextHolder.getLocale()));
    }
  }
  
  private Role createRoleInstance(Role roleDto) throws RightTypeException {
    List<Right> rights = roleDto.getRights().stream().map(rightDto -> rightRepository.findOne(
        rightDto.getId())).collect(toList());

    return new Role(roleDto.getName(),
        roleDto.getDescription(),
        rights.toArray(new Right[roleDto.getRights().size()]));
  }
}

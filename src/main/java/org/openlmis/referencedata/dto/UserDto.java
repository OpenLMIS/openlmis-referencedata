package org.openlmis.referencedata.dto;

import static java.util.stream.Collectors.toSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.User;

import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseDto implements User.Exporter, User.Importer {

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String firstName;

  @Getter
  @Setter
  private String lastName;

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private String timezone;

  @Setter
  private String homeFacilityCode;

  @Getter
  @Setter
  private Facility homeFacility;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  @Getter
  @Setter
  private Set<RoleAssignmentDto> roleAssignments;

  public String fetchHomeFacilityCode() {
    return homeFacilityCode;
  }

  /**
   * Copy role assignments to DTO.
   */
  public void addRoleAssignments(Set<RoleAssignment> roleAssignments) {
    this.roleAssignments = roleAssignments.stream()
        .map(roleAssignment -> exportToDto(roleAssignment))
        .collect(toSet());
  }

  private <T extends RoleAssignment> RoleAssignmentDto exportToDto(T roleAssignment) {
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    if (roleAssignment instanceof SupervisionRoleAssignment) {
      ((SupervisionRoleAssignment) roleAssignment).export(roleAssignmentDto);
    } else if (roleAssignment instanceof FulfillmentRoleAssignment) {
      ((FulfillmentRoleAssignment) roleAssignment).export(roleAssignmentDto);
    } else {
      ((DirectRoleAssignment) roleAssignment).export(roleAssignmentDto);
    }
    return roleAssignmentDto;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UserDto)) {
      return false;
    }
    UserDto userDto = (UserDto) obj;
    return Objects.equals(username, userDto.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}

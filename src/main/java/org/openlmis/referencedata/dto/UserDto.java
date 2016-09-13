package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseEntity implements User.Exporter {

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

  @Setter
  private UUID supervisedNode;

  @Getter
  @Setter
  private String timezone;

  @Getter
  @Setter
  private UUID homeFacilityId;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  @Getter
  private Set<RoleAssignmentDto> roleAssignments = new HashSet<>();
  
  /**
   * Return converted UserDto from User.
   */
  public static UserDto convertUserToUserDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setEmail(user.getEmail());
    userDto.setActive(user.getActive());
    userDto.setFirstName(user.getFirstName());
    userDto.setVerified(user.getVerified());
    userDto.setUsername(user.getUsername());
    userDto.setLastName(user.getLastName());
    userDto.setSupervisedNode(null);
    if (user.getTimezone() != null) {
      userDto.setTimezone(user.getTimezone());
    }
    if (user.getHomeFacility() != null) {
      userDto.setHomeFacilityId(user.getHomeFacility().getId());
    }
    return userDto;
  }

  /**
   * Return converted User from UserDto.
   */
  public static User convertUserDtoToUser(UserDto userDto) {
    User user = new User();
    user.setId(userDto.getId());
    user.setEmail(userDto.getEmail());
    user.setActive(userDto.isActive());
    user.setFirstName(userDto.getFirstName());
    user.setVerified(userDto.isVerified());
    user.setUsername(userDto.getUsername());
    user.setLastName(userDto.getLastName());
    if (user.getTimezone() != null) {
      user.setTimezone(userDto.getTimezone());
    }
    return user;
  }

  @Override
  public RoleAssignment.Exporter provideRoleAssignmentExporter() {
    return new RoleAssignmentDto();
  }

  @Override
  public void addRoleAssignment(RoleAssignment.Exporter roleAssignmentExporter) {
    roleAssignments.add((RoleAssignmentDto)roleAssignmentExporter);
  }
}

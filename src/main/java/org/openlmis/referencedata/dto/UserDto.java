package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseEntity {

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
  private UUID homeFacility;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  @Getter
  private UUID roleAssignments;

  public SupervisoryNode getSupervisedNode() {
    return null;
  }


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
      userDto.setHomeFacility(user.getHomeFacility().getId());
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

}

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

package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.web.csv.model.ImportField;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class UserDto extends UserContactDetailsDto implements User.Exporter, User.Importer {
  @ImportField(name = "username")
  private String username;

  @ImportField(name = "firstName")
  private String firstName;

  @ImportField(name = "lastName")
  private String lastName;

  @ImportField(name = "jobTitle")
  private String jobTitle;

  @ImportField(name = "timezone")
  private String timezone;

  private UUID homeFacilityId;

  @ImportField(name = "homeFacilityCode")
  private String homeFacilityCode;

  @ImportField(name = "isActive")
  private boolean active;

  private Map<String, Object> extraData;

  private Set<RoleAssignmentDto> roleAssignments = Sets.newHashSet();

  public UserAuthDetailsApiContract toUserAuthDetailsApiContract(String defaultPassword) {
    return new UserAuthDetailsApiContract(this.getId(), this.getUsername(),
        defaultPassword, this.isActive());
  }

  /**
   * Creates a new instance of UserDTO based on passed {@link User}.
   *
   * @param user user object from which UserDTO is created
   * @return UserDTO object
   */
  public static UserDto newInstance(User user) {
    UserDto dto = new UserDto();
    user.export(dto);
    return dto;
  }

  /**
   * Creates a new list of UserDto based on given list of {@link User}.
   *
   * @param users list of users
   * @return new list of UserDto
   */
  public static List<UserDto> newInstances(Iterable<User> users) {
    List<UserDto> userDtos = new LinkedList<>();
    users.forEach(u -> userDtos.add(newInstance(u)));
    return userDtos;
  }

  @AllArgsConstructor
  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class UserAuthDetailsApiContract {
    private UUID id;
    private String username;
    private String password;
    private Boolean enabled;
  }
}

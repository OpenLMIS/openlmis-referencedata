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

import java.util.Map;
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
  private FacilityDto homeFacility;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  @Getter
  @Setter
  private boolean loginRestricted;

  @Getter
  @Setter
  private Boolean allowNotify;

  @Getter
  @Setter
  private Map<String, String> extraData;

  @Getter
  @Setter
  private Set<RoleAssignmentDto> roleAssignments;

  public String fetchHomeFacilityCode() {
    return homeFacilityCode;
  }

  @Override
  public void setHomeFacility(Facility homeFacility) {
    this.homeFacility = new FacilityDto();
    homeFacility.export(this.homeFacility);
  }

  /**
   * Copy role assignments to DTO.
   */
  @Override
  public void addRoleAssignments(Set<RoleAssignment> roleAssignments) {
    this.roleAssignments = roleAssignments.stream()
        .map(this::exportToDto)
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

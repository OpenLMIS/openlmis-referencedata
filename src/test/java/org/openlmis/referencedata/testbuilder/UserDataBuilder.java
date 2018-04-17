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

package org.openlmis.referencedata.testbuilder;

import org.openlmis.referencedata.domain.RightAssignment;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserDataBuilder {

  private static int instanceNumber = 0;

  private UUID id = UUID.randomUUID();
  private String username = "admin";
  private String firstName = "Admin";
  private String lastName = "User";
  private String email = "example@mail.com";
  private String timezone = "UTC";
  private UUID homeFacilityId = null;
  private boolean verified = true;
  private boolean active = true;
  private boolean loginRestricted = false;
  private Boolean allowNotify = true;
  private Set<RoleAssignment> roleAssignments = new HashSet<>();
  private Map<String, String> extraData = new HashMap<>();
  private Set<RightAssignment> rightAssignments = new HashSet<>();


  /**
   * Builds instance of {@link UserDataBuilder} with sample data.
   */
  public UserDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    username = "admin" + instanceNumber;
    firstName = "Admin";
    lastName = "User";
    email = instanceNumber + "example@mail.com";
    timezone = "UTC";
    homeFacilityId = null;
    verified = true;
    active = true;
    loginRestricted = false;
    allowNotify = true;
    roleAssignments = new HashSet<>();
    extraData = new HashMap<>();
    rightAssignments = new HashSet<>();
  }

  /**
   * Builds instance of {@link User} without id.
   */
  public User buildAsNew() {
    return new User(
        username, firstName, lastName, email, timezone, homeFacilityId, verified, active,
        loginRestricted, allowNotify, roleAssignments, extraData, rightAssignments
    );
  }

  /**
   * Builds instance of {@link User}.
   */
  public User build() {
    User user = buildAsNew();
    user.setId(id);

    return user;
  }

}

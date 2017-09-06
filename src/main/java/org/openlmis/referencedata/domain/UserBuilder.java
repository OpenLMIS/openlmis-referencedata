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

package org.openlmis.referencedata.domain;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserBuilder {

  private UUID id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String timezone;
  private UUID homeFacilityId;
  private boolean active;
  private boolean verified;
  private boolean loginRestricted;
  private boolean allowNotify;
  private Map<String, String> extraData;

  private UserBuilder() {
    this.homeFacilityId = null;
    this.active = false;
    this.verified = false;
    this.allowNotify = true;
  }

  /**
   * Constructor.
   */
  public UserBuilder(String username, String firstName, String lastName, String email) {
    this();
    this.username = Objects.requireNonNull(username);
    this.firstName = Objects.requireNonNull(firstName);
    this.lastName = Objects.requireNonNull(lastName);
    this.email = Objects.requireNonNull(email);
  }

  public UserBuilder setId(UUID id) {
    this.id = id;
    return this;
  }

  public UserBuilder setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public UserBuilder setHomeFacilityId(UUID homeFacilityId) {
    this.homeFacilityId = homeFacilityId;
    return this;
  }

  public UserBuilder setActive(boolean active) {
    this.active = active;
    return this;
  }

  public UserBuilder setVerified(boolean verified) {
    this.verified = verified;
    return this;
  }

  public UserBuilder setLoginRestricted(boolean loginRestricted) {
    this.loginRestricted = loginRestricted;
    return this;
  }

  public UserBuilder setAllowNotify(Boolean allowNotify) {
    this.allowNotify = allowNotify;
    return this;
  }

  public UserBuilder setExtraData(Map<String, String> extraData) {
    this.extraData = extraData;
    return this;
  }

  public User createUser() {
    return new User(id, username, firstName, lastName, email, timezone, homeFacilityId, active,
        verified, loginRestricted, allowNotify, extraData);
  }
}
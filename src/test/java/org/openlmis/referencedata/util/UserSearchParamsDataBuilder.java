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

package org.openlmis.referencedata.util;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import org.openlmis.referencedata.service.UserSearchParams;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserSearchParamsDataBuilder {

  private Set<String> id = emptySet();
  private String username = "someName";
  private String firstName = "firstName";
  private String lastName = "lastName";
  private String email = "someName@mail.com";
  private String homeFacilityId = UUID.randomUUID().toString();
  private Boolean verified = true;
  private Boolean active = true;
  private Boolean loginRestricted = true;
  private Map<String, String> extraData = emptyMap();

  /**
   * Builds new instance of {@link UserSearchParams} with test data.
   */
  public UserSearchParams build() {
    return new UserSearchParams(id, username, firstName, lastName, email, homeFacilityId, verified,
        active, loginRestricted, extraData);
  }

  /**
   * Sets username for new {@link UserSearchParams}.
   */
  public UserSearchParamsDataBuilder withUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * Sets firstName for new {@link UserSearchParams}.
   */
  public UserSearchParamsDataBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Sets username for new {@link UserSearchParams}.
   */
  public UserSearchParamsDataBuilder withExtraData(Map<String, String> extraData) {
    this.extraData = extraData;
    return this;
  }
}

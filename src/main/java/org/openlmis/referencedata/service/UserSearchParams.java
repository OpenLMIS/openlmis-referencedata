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

package org.openlmis.referencedata.service;

import static java.util.Objects.isNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class UserSearchParams {

  Set<String> id;
  String username;
  String firstName;
  String lastName;
  String email;
  String homeFacilityId;
  Boolean verified;
  Boolean active;
  Boolean loginRestricted;
  Map<String, String> extraData;

  /**
   * Constructor with id.
   */
  public UserSearchParams(Set<String> id) {
    this.id = id;
  }

  /**
   * Constructor with firstName.
   */
  public UserSearchParams(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Check if all params are empty.
   */
  public boolean isEmpty() {
    return CollectionUtils.isEmpty(id) && isNull(username) && isNull(firstName) && isNull(email)
        && isNull(lastName) && isNull(homeFacilityId) && isNull(verified)
        && isNull(active) && isNull(loginRestricted) && MapUtils.isEmpty(extraData);
  }

  /**
   * Gets a set of string id list parsed to UUIDs.
   */
  public Set<UUID> getIds() {
    if (id == null) {
      return Collections.emptySet();
    }
    Set<UUID> ids = new HashSet<>();
    id.forEach(id -> ids.add(UUID.fromString((String)id)));

    return ids;
  }

  /**
   * Gets home facility id parsed to UUID.
   *
   * @return home facility id parsed to UUID
   * @throws ValidationMessageException when home facility id is not valid UUID.
   */
  public UUID getHomeFacilityUuid() {
    if (this.homeFacilityId == null) {
      return null;
    }
    Optional<UUID> uuid = UuidUtil.fromString(this.homeFacilityId);
    if (!uuid.isPresent()) {
      throw new ValidationMessageException(UserMessageKeys.ERROR_HOME_FACILITY_ID_INVALID);
    }
    return uuid.get();
  }

}

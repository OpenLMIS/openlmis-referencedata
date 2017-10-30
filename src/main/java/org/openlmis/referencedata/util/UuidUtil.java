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

import org.springframework.util.MultiValueMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Generic utility functions for {@link UUID}.
 */
public class UuidUtil {

  private static final String ID = "id";

  /**
   * Parses a String into a UUID safely.  Unlike {@link UUID#fromString(String)} however, this
   * method will either return an Optional with the UUID, or it will return an empty Optional
   * in case the String can't be parsed into a UUID.
   *
   * @param uuid see {@link UUID#fromString(String)}.
   * @return An {@link Optional} with either the UUID as parsed from the paramater, or an empty
   *     Optional should it be un-parseable.
   */
  public static Optional<UUID> fromString(String uuid) {
    try {
      return Optional.of(UUID.fromString(uuid));
    } catch (IllegalArgumentException iae) {
      return Optional.empty();
    }
  }

  /**
   * Gets a set of UUIDs from query multi value map. Returns empty set if no {@code id} key.
   *
   * @param queryMap a multi value map that should contain {@code id} as key
   *                 and some UUID string as value.
   * @return a set of {@link UUID} extracted from map where key is {@code id}.
   * @throws ClassCastException when value for key {@code id} is not {@code String}
   * @throws IllegalArgumentException when value for key {@code id} is not parsable to {@link UUID}
   */
  public static Set<UUID> getIds(MultiValueMap<String, Object> queryMap) {
    Set<UUID> ids = new HashSet<>();
    queryMap.forEach((key, value) -> {
      if (Objects.equals(key, ID)) {
        value.forEach(id -> ids.add(UUID.fromString((String)id)));
      }
    });

    return ids;
  }
}
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

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.TypeName;

/**
 * A single per-user preference, stored as a generic key-value pair so that the reference data
 * service stays agnostic of what the preference means (the semantics live in the UI). The unique
 * (userId, preferenceKey) constraint guarantees one value per key per user.
 */
@Entity
@Table(name = "user_preferences", schema = "referencedata",
    uniqueConstraints = @UniqueConstraint(name = "user_preferences_userid_key_unique",
        columnNames = {"userid", "preferencekey"}))
@NoArgsConstructor
@TypeName("UserPreference")
public class UserPreference extends BaseEntity {

  @Column(name = "userid", nullable = false)
  @Type(type = UUID_TYPE)
  @Getter
  @Setter
  private UUID userId;

  @Column(name = "preferencekey", nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String preferenceKey;

  @Column(name = "preferencevalue", nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String preferenceValue;

  /**
   * Creates a new preference for the given user.
   *
   * @param userId          the owner of the preference
   * @param preferenceKey   the preference name
   * @param preferenceValue the preference value
   */
  public UserPreference(UUID userId, String preferenceKey, String preferenceValue) {
    this.userId = userId;
    this.preferenceKey = preferenceKey;
    this.preferenceValue = preferenceValue;
  }
}

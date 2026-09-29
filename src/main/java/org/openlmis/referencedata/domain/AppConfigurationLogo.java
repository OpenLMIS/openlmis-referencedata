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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The bytes of an uploaded logo, keyed by their SHA-256. Kept apart from
 * {@link AppConfiguration} so its audit snapshots never hold the image.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_configuration_logos")
public class AppConfigurationLogo {

  @Id
  private String sha256;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private byte[] data;
}

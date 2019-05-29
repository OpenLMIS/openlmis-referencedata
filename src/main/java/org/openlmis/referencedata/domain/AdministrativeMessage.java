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

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "administrative_messages")
public class AdministrativeMessage extends BaseEntity {

  @Getter
  @Setter
  private String title;

  @Column(nullable = false)
  @Getter
  @Setter
  private String message;

  @Getter
  @Setter
  private ZonedDateTime startDate;

  @Getter
  @Setter
  private ZonedDateTime expiryDate;

  @Column(nullable = false)
  @Getter
  @Setter
  private ZonedDateTime createdDate;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean active;

  @ManyToOne
  @JoinColumn(name = "authorid")
  @Getter
  @Setter
  private User author;

}

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

import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lots", schema = "referencedata")
@Getter
@Setter
public class Lot extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  private String lotCode;

  @Column(columnDefinition = "timestamp with time zone")
  private ZonedDateTime expirationDate;

  @Column(columnDefinition = "timestamp with time zone")
  private ZonedDateTime manufactureDate;

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(nullable = false, name = "tradeitemid")
  private TradeItem tradeItem;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  private boolean active;
}

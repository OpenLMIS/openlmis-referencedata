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

import java.util.UUID;
import org.openlmis.referencedata.domain.UnitOfOrderable;

public class UnitOfOrderableBuilder {
  private static int instanceNumber = 0;

  private UUID id;
  private String name;
  private String description;
  private Integer displayOrder;
  private Integer factor;

  /**
   * New instance of UnitOfOrderableBuilder.
   */
  public UnitOfOrderableBuilder() {
    ++instanceNumber;

    id = UUID.randomUUID();
    name = "UnitOfOrderable #" + instanceNumber;
    description = "A test Unit Of Orderable, instanceNumber=" + instanceNumber;
    displayOrder = instanceNumber;
    factor = 1;
  }

  public UnitOfOrderableBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public UnitOfOrderableBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public UnitOfOrderableBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public UnitOfOrderableBuilder withDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  public UnitOfOrderableBuilder withFactor(Integer factor) {
    this.factor = factor;
    return this;
  }

  /**
   * Builds new instance of UnitOfOrderable with set ID.
   *
   * @return an UnitOfOrderable with set Id
   */
  public UnitOfOrderable build() {
    UnitOfOrderable unit = buildAsNew();
    unit.setId(id);
    return unit;
  }

  /**
   * Builds UnitOfOrderable without ID.
   *
   * @return an UnitOfOrderable with null Id
   */
  public UnitOfOrderable buildAsNew() {
    return new UnitOfOrderable(name, description, displayOrder, factor);
  }
}

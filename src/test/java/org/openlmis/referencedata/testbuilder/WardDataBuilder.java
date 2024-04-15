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
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Ward;

public class WardDataBuilder {

  private static int instanceNumber = 0;

  private Facility facility;
  private UUID id;
  private String name;
  private String description;
  private boolean disabled;
  private Code code;

  /**
   * Builds instance of {@link WardDataBuilder} with sample data.
   */
  public WardDataBuilder() {
    id = UUID.randomUUID();
    name = "Ward " + instanceNumber;
    description = "Test ward";
    disabled = false;
    code = Code.code("W" + instanceNumber);
    facility = new FacilityDataBuilder().build();
  }

  /**
   * Builds instance of {@link Ward}.
   */
  public Ward buildAsNew() {
    Ward ward = new Ward(facility, name, description, disabled, code);

    return ward;
  }

  /**
   * Builds instance of {@link Ward}.
   */
  public Ward build() {
    Ward ward = buildAsNew();
    ward.setId(id);

    return ward;
  }

  /**
   * Sets id for new {@link Ward}.
   */
  public WardDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Sets null id for new {@link Ward}.
   */
  public WardDataBuilder withoutId() {
    return withId(null);
  }

  public WardDataBuilder withCode(String code) {
    this.code = Code.code(code);
    return this;
  }

  /**
   * Sets name for new {@link Ward}.
   */
  public WardDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets description for new {@link Ward}.
   */
  public WardDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets disabled flag for new {@link Ward}.
   */
  public WardDataBuilder withDisabledFlag(boolean disabled) {
    this.disabled = disabled;
    return this;
  }

  /**
   * Sets facility for new {@link Ward}.
   */
  public WardDataBuilder withFacility(Facility facility) {
    this.facility = facility;
    return this;
  }

}

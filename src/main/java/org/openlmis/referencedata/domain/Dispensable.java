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

import javax.persistence.Embeddable;

/**
 * A Dispensable describes how product is dispensed/given to a patient.
 * Description of the Dispensable contains information about product form,
 * dosage, dispensing unit etc.
 */
@Embeddable
public class Dispensable {

  @Getter
  private final String dispensingUnit;

  protected Dispensable() {
    this.dispensingUnit = "";
  }

  private Dispensable(String dispensingUnit) {
    this.dispensingUnit = dispensingUnit.trim();
  }

  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof Dispensable)) {
      return false;
    }

    return this.dispensingUnit.equalsIgnoreCase(((Dispensable) object).dispensingUnit);
  }

  @Override
  public final int hashCode() {
    return dispensingUnit.toLowerCase().hashCode();
  }

  @Override
  public String toString() {
    return dispensingUnit;
  }

  public static Dispensable createNew(String dispensingUnit) {
    String correctDispensingUnit = (null == dispensingUnit) ? "" : dispensingUnit;
    return new Dispensable(correctDispensingUnit);
  }

}

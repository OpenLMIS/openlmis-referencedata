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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * A Dispensable describes how product is dispensed/given to a patient.
 * Description of the Dispensable contains information about product form,
 * dosage, dispensing unit etc.
 */
@Entity
@Table(name = "dispensables")
public class Dispensable extends BaseEntity {

  private static final String KEY_DISPENSING_UNIT = "dispensingUnit";

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "key")
  @Column(name = "value")
  @CollectionTable(
      name = "dispensable_attributes",
      joinColumns = @JoinColumn(name = "dispensableid"))
  private Map<String, String> attributes;

  protected Dispensable() {
    attributes = new HashMap<>();
  }

  private Dispensable(String dispensingUnit) {
    this();
    attributes.put(KEY_DISPENSING_UNIT, dispensingUnit);
  }

  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof Dispensable)) {
      return false;
    }

    return this.attributes.get(KEY_DISPENSING_UNIT)
        .equalsIgnoreCase(((Dispensable) object).attributes.get(KEY_DISPENSING_UNIT));
  }

  @Override
  public final int hashCode() {
    return attributes.hashCode();
  }

  @Override
  public String toString() {
    return attributes.getOrDefault(KEY_DISPENSING_UNIT, "");
  }

  public static Dispensable createNew(String dispensingUnit) {
    String correctDispensingUnit = (null == dispensingUnit) ? "" : dispensingUnit;
    return new Dispensable(correctDispensingUnit);
  }

  /**
   * Creates new instance based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of Dispensable.
   */
  public static Dispensable newInstance(Importer importer) {
    if (importer == null) {
      return new Dispensable();
    }
    return new Dispensable(importer.getDispensingUnit());
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setDispensingUnit(attributes.get(KEY_DISPENSING_UNIT));
  }

  public interface Exporter {
    void setDispensingUnit(String dispensingUnit);
  }

  public interface Importer {
    String getDispensingUnit();
  }
}

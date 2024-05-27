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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;

/** The entity represents an unit in which Orderable items can be counted. */
@Entity
@TypeName("UnitOfOrderable")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unit_of_orderables", schema = "referencedata")
public class UnitOfOrderable extends BaseEntity {
  public static final String TEXT = "text";

  @Column(columnDefinition = TEXT, nullable = false)
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;

  /** A number which defines a display order, lower values should appear higher. */
  @Column(nullable = false)
  @Getter
  @Setter
  private Integer displayOrder;

  /** A number to multiple a value in this Unit by, to get an absolute count of Orderable items. */
  @Column(nullable = false)
  @Getter
  @Setter
  private Integer factor;

  /**
   * Creates new instance of {@link UnitOfOrderable}.
   *
   * @param importer the instance of {@link Importer}, not null
   * @return new instance of Unit Of Orderable, never null
   */
  public static UnitOfOrderable newInstance(Importer importer) {
    UnitOfOrderable unit = new UnitOfOrderable();
    unit.setId(importer.getId());
    unit.setName(importer.getName());
    unit.setDescription(importer.getDescription());
    unit.setDisplayOrder(importer.getDisplayOrder());
    unit.setFactor(importer.getFactor());
    return unit;
  }

  /**
   * Creates a list of new instances of UnitOfOrderable.
   *
   * @param importers the list of unit importers, not null
   * @return the list of new instances, never null
   */
  public static <T extends Importer> List<UnitOfOrderable> newInstances(List<T> importers) {
    return importers != null
        ? importers.stream().map(UnitOfOrderable::newInstance).collect(Collectors.toList())
        : Collections.emptyList();
  }

  /**
   * Updates this instance from the given one.
   *
   * @param unitOfOrderable the instance of {@link UnitOfOrderable}
   */
  public void updateFrom(UnitOfOrderable unitOfOrderable) {
    this.setName(unitOfOrderable.getName());
    this.setDescription(unitOfOrderable.getDescription());
    this.setDisplayOrder(unitOfOrderable.getDisplayOrder());
    this.setFactor(unitOfOrderable.getFactor());
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(getName());
    exporter.setDescription(getDescription());
    exporter.setDisplayOrder(getDisplayOrder());
    exporter.setFactor(getFactor());
  }

  public interface Exporter extends BaseExporter {
    void setName(String name);

    void setDescription(String description);

    void setDisplayOrder(Integer displayOrder);

    void setFactor(Integer factor);
  }

  public interface Importer extends BaseImporter {
    String getName();

    String getDescription();

    Integer getDisplayOrder();

    Integer getFactor();
  }
}

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Table(name = "facility_type_approved_products", schema = "referencedata",
    uniqueConstraints = @UniqueConstraint(name = "unq_ftap",
        columnNames = { "orderableId", "programId", "facilityTypeId" }))
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TypeName("FacilityTypeApprovedProduct")
public class FacilityTypeApprovedProduct extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "orderableId", nullable = false)
  @Getter
  @Setter
  private Orderable orderable;

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "facilityTypeId", nullable = false)
  @Getter
  @Setter
  private FacilityType facilityType;

  @Column(nullable = false)
  @Getter
  @Setter
  private Double maxPeriodsOfStock;

  @Getter
  @Setter
  private Double minPeriodsOfStock;

  @Getter
  @Setter
  private Double emergencyOrderPoint;

  /**
   * Creates new FacilityTypeApprovedProduct based on data
   * from {@link FacilityTypeApprovedProduct.Importer}
   *
   * @param importer instance of {@link FacilityTypeApprovedProduct.Importer}
   * @return new instance of FacilityTypeApprovedProduct.
   */
  public static FacilityTypeApprovedProduct newFacilityTypeApprovedProduct(Importer importer) {
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setId(importer.getId());
    ftap.setMaxPeriodsOfStock(importer.getMaxPeriodsOfStock());
    ftap.setMinPeriodsOfStock(importer.getMinPeriodsOfStock());
    ftap.setEmergencyOrderPoint(importer.getEmergencyOrderPoint());
    return ftap;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setMaxPeriodsOfStock(maxPeriodsOfStock);
    exporter.setMinPeriodsOfStock(minPeriodsOfStock);
    exporter.setEmergencyOrderPoint(emergencyOrderPoint);
    exporter.setOrderable(orderable);
    exporter.setProgram(program);
    exporter.setFacilityType(facilityType);
  }

  public interface Exporter extends BaseExporter {

    void setMaxPeriodsOfStock(Double maxPeriodsOfStock);

    void setMinPeriodsOfStock(Double minPeriodsOfStock);

    void setEmergencyOrderPoint(Double emergencyOrderPoint);

    void setOrderable(Orderable orderable);

    void setProgram(Program program);

    void setFacilityType(FacilityType facilityType);
  }

  public interface Importer extends BaseImporter {

    Double getMaxPeriodsOfStock();

    Double getMinPeriodsOfStock();

    Double getEmergencyOrderPoint();

    Orderable.Importer getOrderable();

    Program.Importer getProgram();

    FacilityType.Importer getFacilityType();
  }
}

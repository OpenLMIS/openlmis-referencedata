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
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "facility_type_approved_products", schema = "referencedata")
@NoArgsConstructor
public class FacilityTypeApprovedProduct extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "facilityTypeId", nullable = false)
  @Getter
  @Setter
  private FacilityType facilityType;

  @ManyToOne
  @JoinColumn(name = "programOrderableId", nullable = false)
  @Getter
  @Setter
  private ProgramOrderable programOrderable;

  @Column(nullable = false)
  @Getter
  @Setter
  private Double maxPeriodsOfStock;

  @Column
  @Getter
  @Setter
  private Double minPeriodsOfStock;

  @Column
  @Getter
  @Setter
  private Double emergencyOrderPoint;

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setProgramOrderable(programOrderable);
    exporter.setMaxPeriodsOfStock(maxPeriodsOfStock);
    exporter.setMinPeriodsOfStock(minPeriodsOfStock);
    exporter.setEmergencyOrderPoint(emergencyOrderPoint);
  }

  @Override
  public boolean equals(Object other) {

    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    FacilityTypeApprovedProduct otherFacility = (FacilityTypeApprovedProduct) other;

    if (!facilityType.equals(otherFacility.facilityType)) {
      return false;
    }
    if (!programOrderable.equals(otherFacility.programOrderable)) {
      return false;
    }
    if (!maxPeriodsOfStock.equals(otherFacility.maxPeriodsOfStock)) {
      return false;
    }
    if (minPeriodsOfStock != null ? !minPeriodsOfStock.equals(otherFacility.minPeriodsOfStock) :
        otherFacility.minPeriodsOfStock != null) {
      return false;
    }
    return emergencyOrderPoint != null ? emergencyOrderPoint.equals(
        otherFacility.emergencyOrderPoint) : otherFacility.emergencyOrderPoint == null;

  }

  @Override
  public int hashCode() {
    int result = facilityType.hashCode();
    result = 31 * result + programOrderable.hashCode();
    result = 31 * result + maxPeriodsOfStock.hashCode();
    result = 31 * result + (minPeriodsOfStock != null ? minPeriodsOfStock.hashCode() : 0);
    result = 31 * result + (emergencyOrderPoint != null ? emergencyOrderPoint.hashCode() : 0);
    return result;
  }


  public interface Exporter {
    void setId(UUID id);

    void setProgramOrderable(ProgramOrderable programOrderable);

    void setMaxPeriodsOfStock(Double maxPeriodsOfStock);

    void setMinPeriodsOfStock(Double minPeriodsOfStock);

    void setEmergencyOrderPoint(Double emergencyOrderPoint);
  }

  public interface Importer {
    UUID getId();

    ProgramOrderable.Importer getProgramOrderable();

    Double getMaxPeriodsOfStock();

    Double getMinPeriodsOfStock();

    Double getEmergencyOrderPoint();
  }
}

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
  private Double maxStock;

  @Column
  @Getter
  @Setter
  private Double minStock;

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
    exporter.setMaxStock(maxStock);
    exporter.setMinStock(minStock);
    exporter.setEmergencyOrderPoint(emergencyOrderPoint);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProgramOrderable(ProgramOrderable programOrderable);

    void setMaxStock(Double maxStock);

    void setMinStock(Double minStock);

    void setEmergencyOrderPoint(Double emergencyOrderPoint);
  }

  public interface Importer {
    UUID getId();

    ProgramOrderable.Importer getProgramOrderable();

    Double getMaxStock();

    Double getMinStock();

    Double getEmergencyOrderPoint();
  }
}

package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "facility_types", schema = "referencedata")
@NoArgsConstructor
public class FacilityType extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private Integer displayOrder;

  @Getter
  @Setter
  private Boolean active;

  public FacilityType(String code) {
    this.code = code;
  }

  /**
   * Copy method. TODO: eventually replace with importer/exporter pattern (e.g. Right.Importer and
   * Right.Exporter with export() method and importer constructor.
   */
  public void updateFrom(FacilityType facilityType) {
    this.code = facilityType.code;
    this.name = facilityType.name;
    this.description = facilityType.description;
    this.displayOrder = facilityType.displayOrder;
    this.active = facilityType.active;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FacilityType)) {
      return false;
    }
    FacilityType that = (FacilityType) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }
}

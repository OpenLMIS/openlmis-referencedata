package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

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

  /**
   * Creates new facility type object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of facility type.
   */
  public static FacilityType newFacilityType(Importer importer) {
    FacilityType facilityType = new FacilityType();
    facilityType.setId(importer.getId());
    facilityType.setCode(importer.getCode());
    facilityType.setName(importer.getName());
    facilityType.setDescription(importer.getDescription());
    facilityType.setDisplayOrder(importer.getDisplayOrder());
    facilityType.setActive(importer.getActive());

    return facilityType;
  }

  /**
   * Exports current state of facility type object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setDisplayOrder(displayOrder);
    exporter.setActive(active);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setDisplayOrder(Integer displayOrder);

    void setActive(Boolean active);

  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    Integer getDisplayOrder();

    Boolean getActive();

  }
}

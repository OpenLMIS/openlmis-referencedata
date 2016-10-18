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
@Table(name = "facility_operators", schema = "referencedata")
@NoArgsConstructor
public class FacilityOperator extends BaseEntity {

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FacilityOperator)) {
      return false;
    }
    FacilityOperator that = (FacilityOperator) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  /**
   * Creates new facility operator object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of facility operator.
   */
  public static FacilityOperator newFacilityOperator(Importer importer) {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setId(importer.getId());
    facilityOperator.setCode(importer.getCode());
    facilityOperator.setName(importer.getName());
    facilityOperator.setDescription(importer.getDescription());
    facilityOperator.setDisplayOrder(importer.getDisplayOrder());

    return facilityOperator;
  }

  /**
   * Exports current state of facility operator object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setDisplayOrder(displayOrder);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setDisplayOrder(Integer displayOrder);

  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    Integer getDisplayOrder();

  }
}

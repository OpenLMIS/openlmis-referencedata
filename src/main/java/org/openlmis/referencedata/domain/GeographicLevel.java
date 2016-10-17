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
@Table(name = "geographic_levels", schema = "referencedata")
@NoArgsConstructor
public class GeographicLevel extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer levelNumber;

  public GeographicLevel(String code, int levelNumber) {
    this.code = code;
    this.levelNumber = levelNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GeographicLevel)) {
      return false;
    }
    GeographicLevel that = (GeographicLevel) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  /**
   * Exports current state of geographic level object.
   *
   * @param exporter instance of {@link GeographicLevel.Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setLevelNumber(levelNumber);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setLevelNumber(Integer levelNumber);
  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    Integer getLevelNumber();

  }
}

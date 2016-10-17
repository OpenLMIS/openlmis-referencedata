package org.openlmis.referencedata.domain;

import org.openlmis.referencedata.dto.GeographicLevelDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "geographic_zones", schema = "referencedata")
@NoArgsConstructor
public class GeographicZone extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @ManyToOne
  @JoinColumn(name = "levelid", nullable = false)
  @Getter
  @Setter
  private GeographicLevel level;

  //@ManyToOne TODO: re-enable this at some point, similar to SupervisoryNode
  //@JoinColumn(name = "parentid")
  //@Getter
  //@Setter
  //private GeographicZone parent;

  @Getter
  @Setter
  private Integer catchmentPopulation;

  @Column(columnDefinition = "numeric(8,5)")
  @Getter
  @Setter
  private Double latitude;

  @Column(columnDefinition = "numeric(8,5)")
  @Getter
  @Setter
  private Double longitude;

  public GeographicZone(String code, GeographicLevel level) {
    this.code = code;
    this.level = level;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GeographicZone)) {
      return false;
    }
    GeographicZone that = (GeographicZone) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  /**
   * Exports current state of geographic zone object.
   *
   * @param exporter instance of {@link GeographicZone.Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);

    if (null != level) {
      GeographicLevelDto levelDto = new GeographicLevelDto();
      level.export(levelDto);

      exporter.setLevel(levelDto);
    }

    exporter.setCatchmentPopulation(catchmentPopulation);
    exporter.setLatitude(latitude);
    exporter.setLongitude(longitude);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setLevel(GeographicLevelDto level);

    void setCatchmentPopulation(Integer catchmentPopulation);

    void setLatitude(Double latitude);

    void setLongitude(Double longitude);

  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    GeographicLevelDto getLevel();

    Integer getCatchmentPopulation();

    Double getLatitude();

    Double getLongitude();

  }
}

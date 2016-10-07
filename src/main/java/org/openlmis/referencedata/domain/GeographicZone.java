package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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
}

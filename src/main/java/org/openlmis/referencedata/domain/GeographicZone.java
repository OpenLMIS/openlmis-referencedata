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

import com.vividsolutions.jts.geom.Polygon;

import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.TypeName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "geographic_zones", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
@TypeName("GeographicZone")
@NamedQueries({
    @NamedQuery(name = "GeographicZone.findIdsByParent",
        query = "SELECT DISTINCT id FROM GeographicZone WHERE parent.id = :parentId")
    })
public class GeographicZone extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "levelid", nullable = false)
  @Getter
  @Setter
  private GeographicLevel level;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parentid")
  @Getter
  @Setter
  private GeographicZone parent;

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

  @Type(type = "jts_geometry")
  @Getter
  @Setter
  private Polygon boundary;

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
   * Creates new geographic zone object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of geographic zone.
   */
  public static GeographicZone newGeographicZone(Importer importer) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setId(importer.getId());
    geographicZone.setCode(importer.getCode());
    geographicZone.setName(importer.getName());

    if (null != importer.getLevel()) {
      geographicZone.setLevel(GeographicLevel.newGeographicLevel(importer.getLevel()));
    }

    if (null != importer.getParent()) {
      geographicZone.setParent(newGeographicZone(importer.getParent()));
    }

    geographicZone.setCatchmentPopulation(importer.getCatchmentPopulation());
    geographicZone.setLatitude(importer.getLatitude());
    geographicZone.setLongitude(importer.getLongitude());
    
    geographicZone.setBoundary(importer.getBoundary());

    return geographicZone;
  }

  /**
   * Exports current state of geographic zone object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);

    if (null != level) {
      exporter.setLevel(level);
    }

    if (null != parent) {
      exporter.setParent(parent);
    }

    exporter.setCatchmentPopulation(catchmentPopulation);
    exporter.setLatitude(latitude);
    exporter.setLongitude(longitude);
    exporter.setBoundary(boundary);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setLevel(GeographicLevel level);

    void setCatchmentPopulation(Integer catchmentPopulation);

    void setLatitude(Double latitude);

    void setLongitude(Double longitude);
    
    void setBoundary(Polygon boundary);

    void setParent(GeographicZone parent);

  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    GeographicLevel.Importer getLevel();

    Integer getCatchmentPopulation();

    Double getLatitude();

    Double getLongitude();
    
    Polygon getBoundary();

    Importer getParent();

  }
}

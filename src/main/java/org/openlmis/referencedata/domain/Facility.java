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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@TypeName("Facility")
@Table(name = "facilities", schema = "referencedata")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Facility extends BaseEntity {

  public static final String TEXT = "text";
  public static final String WAREHOUSE_CODE = "warehouse";

  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;

  @ManyToOne
  @JoinColumn(name = "geographiczoneid", nullable = false)
  @Getter
  @Setter
  private GeographicZone geographicZone;

  @ManyToOne
  @JoinColumn(name = "typeid", nullable = false)
  @Getter
  @Setter
  private FacilityType type;

  @ManyToOne
  @JoinColumn(name = "operatedbyid")
  @Getter
  @Setter
  private FacilityOperator operator;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean active;

  @Getter
  @Setter
  private LocalDate goLiveDate;

  @Getter
  @Setter
  private LocalDate goDownDate;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String comment;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean enabled;

  @Getter
  @Setter
  private Boolean openLmisAccessible;

  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  @DiffIgnore
  @Getter
  @Setter
  private Set<SupportedProgram> supportedPrograms = new HashSet<>();

  @Type(type = "jts_geometry")
  @DiffIgnore
  @Getter
  @Setter
  private Point location;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  @Getter
  @Setter
  private Map<String, String> extraData;

  private Facility() {

  }

  public Facility(String code) {
    this.code = code;
  }

  /**
   * Equal by a Facility's code.
   *
   * @param other the other Facility
   * @return true if the two Facilities' {@link Code} are equal.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Facility)) {
      return false;
    }

    Facility facility = (Facility) other;
    return Objects.equals(code, facility.getCode());
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  /**
   * Creates new facility object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of facility.
   */
  public static Facility newFacility(Importer importer) {
    Facility facility = new Facility();
    facility.setId(importer.getId());
    facility.setCode(importer.getCode());
    facility.setName(importer.getName());
    facility.setDescription(importer.getDescription());

    if (null != importer.getGeographicZone()) {
      facility.setGeographicZone(GeographicZone.newGeographicZone(importer.getGeographicZone()));
    }

    if (null != importer.getType()) {
      facility.setType(FacilityType.newFacilityType(importer.getType()));
    }

    if (null != importer.getOperator()) {
      facility.setOperator(FacilityOperator.newFacilityOperator(importer.getOperator()));
    }

    facility.setExtraData(importer.getExtraData());
    facility.setActive(importer.getActive());
    facility.setGoLiveDate(importer.getGoLiveDate());
    facility.setGoDownDate(importer.getGoDownDate());
    facility.setComment(importer.getComment());
    facility.setEnabled(importer.getEnabled());
    facility.setOpenLmisAccessible(importer.getOpenLmisAccessible());
    
    facility.setLocation(importer.getLocation());

    return facility;
  }

  public void addSupportedProgram(SupportedProgram supportedProgram) {
    supportedPrograms.add(supportedProgram);
  }

  /**
   * Exports current state of facility object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);

    if (null != geographicZone) {
      exporter.setGeographicZone(geographicZone);
    }

    if (null != type) {
      exporter.setType(type);
    }

    if (null != operator) {
      exporter.setOperator(operator);
    }

    exporter.setActive(active);
    exporter.setGoLiveDate(goLiveDate);
    exporter.setGoDownDate(goDownDate);
    exporter.setComment(comment);
    exporter.setEnabled(enabled);
    exporter.setOpenLmisAccessible(openLmisAccessible);

    if (null != supportedPrograms) {
      exporter.setSupportedPrograms(supportedPrograms);
    }
    
    exporter.setLocation(location);

    exporter.setExtraData(extraData);
  }

  /**
   * Exports basic fields in the current state of facility object.
   *
   * @param basicExporter instance of {@link BasicExporter}
   */
  public void basicExport(BasicExporter basicExporter) {
    basicExporter.setId(id);
    basicExporter.setCode(code);
    basicExporter.setName(name);
    basicExporter.setActive(active);
    basicExporter.setGeographicZone(geographicZone);
  }

  public boolean isWarehouse() {
    return WAREHOUSE_CODE.equalsIgnoreCase(type.getCode());
  }

  /**
   * Check to see if this facility supports the specified program.
   */
  public boolean supports(Program program) {
    return supportedPrograms
        .stream()
        .anyMatch(supported -> supported.getProgram().equals(program));
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setGeographicZone(GeographicZone geographicZone);

    void setType(FacilityType type);

    void setOperator(FacilityOperator operator);

    void setActive(Boolean active);

    void setGoLiveDate(LocalDate goLiveDate);

    void setGoDownDate(LocalDate goDownDate);

    void setComment(String comment);

    void setEnabled(Boolean enabled);

    void setOpenLmisAccessible(Boolean openLmisAccessible);

    void setSupportedPrograms(Set<SupportedProgram> supportedPrograms);
    
    void setLocation(Point location);

    void setExtraData(Map<String, String> extraData);
  }

  public interface BasicExporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setActive(Boolean active);

    void setEnabled(Boolean enabled);

    void setType(FacilityType type);

    void setGeographicZone(GeographicZone geographicZone);
  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    GeographicZone.Importer getGeographicZone();

    FacilityType.Importer getType();

    FacilityOperator.Importer getOperator();

    Boolean getActive();

    LocalDate getGoLiveDate();

    LocalDate getGoDownDate();

    String getComment();

    Boolean getEnabled();

    Boolean getOpenLmisAccessible();
    
    Point getLocation();
    
    Map<String, String> getExtraData();
  }
}

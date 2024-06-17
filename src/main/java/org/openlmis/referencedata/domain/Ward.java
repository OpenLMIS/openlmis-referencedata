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

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "wards", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
public class Ward extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "facilityId", nullable = false)
  @Getter
  @Setter
  private Facility facility;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(nullable = false, columnDefinition = "text")
  private String description;

  @Column(nullable = false, columnDefinition = "boolean")
  private boolean disabled;

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Embedded
  private Code code;

  /**
   * Creates new ward object based on data from {@link Ward.Importer}.
   *
   * @param importer instance of {@link Ward.Importer}
   * @return new instance of ward.
   */
  public static Ward newWard(Importer importer) {
    Ward ward = new Ward();
    if (importer.getFacility() != null) {
      ward.setFacility(Facility.newFacility(importer.getFacility()));
    }
    ward.setId(importer.getId());
    ward.updateFrom(importer);

    return ward;
  }

  /**
   * Updates data based on data from {@link Ward.Importer}.
   *
   * @param importer instance of {@link Ward.Importer}
   */
  public void updateFrom(Ward.Importer importer) {
    name = importer.getName();
    description = importer.getDescription();
    disabled = importer.isDisabled();
    code = Code.code(importer.getCode());
  }

  /**
   * Exports current state of ward object.
   *
   * @param exporter instance of {@link Ward.Exporter}
   */
  public void export(Ward.Exporter exporter) {
    exporter.setId(id);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setDisabled(disabled);

    String codeString = this.code.toString();
    if (isFalse(codeString.isEmpty())) {
      exporter.setCode(codeString);
    }

    Optional<Facility.Exporter> exporterOptional =
        exporter.provideFacilityExporter();
    if (exporterOptional.isPresent()) {
      Facility.Exporter facilityExporter = exporterOptional.get();
      facility.export(facilityExporter);
      exporter.includeFacility(facilityExporter);
    }
  }

  /**
   * Equal by a Ward's code.
   *
   * @param other the other Ward
   * @return true if the two Ward's {@link Code} are equal.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Ward)) {
      return false;
    }

    Ward otherWard = (Ward) other;
    return code.equals(otherWard.code);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(code);
  }

  public interface Exporter extends BaseExporter {
    void setName(String name);

    void setDescription(String description);

    void setDisabled(boolean disabled);

    void setCode(String code);

    Optional<Facility.Exporter> provideFacilityExporter();

    void includeFacility(Facility.Exporter facilityExporter);

  }

  public interface Importer extends BaseImporter {
    String getName();

    String getDescription();

    boolean isDisabled();

    String getCode();

    Facility.Importer getFacility();

  }

}

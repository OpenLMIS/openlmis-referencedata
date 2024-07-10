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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;

@Setter
@Getter
@Entity
@TypeName("IntegrationEmail")
@Table(name = "integration_emails", schema = "referencedata")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IntegrationEmail extends BaseEntity {

  @Column(nullable = false, columnDefinition = "text")
  private String email;

  /**
   * Creates new instance based on data from the importer.
   */
  public static IntegrationEmail newInstance(Importer importer) {
    IntegrationEmail integrationEmail = new IntegrationEmail();
    integrationEmail.setId(importer.getId());
    integrationEmail.updateFrom(importer);

    return integrationEmail;
  }

  public void updateFrom(Importer importer) {
    email = importer.getEmail();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setEmail(email);
  }

  public interface Exporter extends BaseExporter {

    void setEmail(String name);

  }

  public interface Importer extends BaseImporter {

    String getEmail();

  }

}

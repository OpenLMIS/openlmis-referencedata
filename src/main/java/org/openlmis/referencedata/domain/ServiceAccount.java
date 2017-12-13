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

import org.javers.core.metamodel.annotation.TypeName;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@TypeName("ServiceAccount")
@Table(name = "service_accounts", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public final class ServiceAccount extends BaseEntity {

  @Column(nullable = false)
  private String login;

  @Embedded
  private CreationDetails creationDetails;

  /**
   * Creates new service account object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of service account.
   */
  public static ServiceAccount newServiceAccount(Importer importer) {
    CreationDetails creationDetails = new CreationDetails(
        importer.getCreatedBy(), importer.getCreatedDate()
    );

    ServiceAccount account = new ServiceAccount(importer.getLogin(), creationDetails);
    account.setId(importer.getId());

    return account;
  }

  /**
   * Exports current state of service account object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setLogin(login);
    exporter.setCreatedBy(creationDetails.getCreatedBy());
    exporter.setCreatedDate(creationDetails.getCreatedDate());
  }

  public interface Exporter {

    void setId(UUID id);

    void setLogin(String login);

    void setCreatedBy(UUID createdBy);

    void setCreatedDate(ZonedDateTime createdDate);

  }

  public interface Importer {

    UUID getId();

    String getLogin();

    UUID getCreatedBy();

    ZonedDateTime getCreatedDate();

  }
}

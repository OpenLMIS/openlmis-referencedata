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

package org.openlmis.referencedata.testbuilder;

import java.util.UUID;
import org.openlmis.referencedata.domain.IntegrationEmail;

public class IntegrationEmailDataBuilder {

  private UUID id = UUID.randomUUID();
  private String email = "my@email.com";

  public IntegrationEmailDataBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  /**
   * Builds new instance of IntegrationEmail (with id field).
   */
  public IntegrationEmail build() {
    IntegrationEmail integrationEmail = buildAsNew();
    integrationEmail.setId(id);

    return integrationEmail;
  }

  /**
   * Builds new instance of IntegrationEmail as a new object (without id field).
   */
  public IntegrationEmail buildAsNew() {
    IntegrationEmail integrationEmail = new IntegrationEmail();
    integrationEmail.setEmail(email);

    return integrationEmail;
  }

}

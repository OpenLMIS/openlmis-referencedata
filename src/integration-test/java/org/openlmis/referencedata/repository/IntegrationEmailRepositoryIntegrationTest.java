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

package org.openlmis.referencedata.repository;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.IntegrationEmail;
import org.openlmis.referencedata.testbuilder.IntegrationEmailDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class IntegrationEmailRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<IntegrationEmail> {

  @Autowired
  private IntegrationEmailRepository integrationEmailRepository;

  @Override
  CrudRepository<IntegrationEmail, UUID> getRepository() {
    return integrationEmailRepository;
  }

  @Override
  IntegrationEmail generateInstance() {
    return new IntegrationEmailDataBuilder()
        .withEmail("test" + getNextInstanceNumber() + "@email.com")
        .buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowForSeveralIntegrationEmailsWithSameEmail() {
    IntegrationEmail integrationEmail1 = generateInstance();
    IntegrationEmail integrationEmail2 = generateInstance();
    integrationEmail2.setEmail(integrationEmail1.getEmail());

    integrationEmailRepository.saveAndFlush(integrationEmail1);
    integrationEmailRepository.saveAndFlush(integrationEmail2);
  }

}

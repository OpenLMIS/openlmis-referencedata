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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.testbuilder.ServiceAccountDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public class ServiceAccountRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<ServiceAccount> {

  @Autowired
  private ServiceAccountRepository repository;

  private ServiceAccount account;

  @Before
  public void setUp() {
    account = new ServiceAccountDataBuilder().build();
  }

  @Override
  CrudRepository<ServiceAccount, UUID> getRepository() {
    return repository;
  }

  @Override
  ServiceAccount generateInstance() {
    return account;
  }

  @Override
  protected void assertBefore(ServiceAccount instance) {
    assertThat(instance.getApiKeyId(), is(notNullValue()));
  }

  @Override
  protected void assertInstance(ServiceAccount instance) {
    assertThat(instance.getApiKeyId(), is(equalTo(account.getApiKeyId())));
    assertThat(
        instance.getCreationDetails().getCreatedBy(),
        is(equalTo(account.getCreationDetails().getCreatedBy()))
    );
    assertThat(
        instance.getCreationDetails().getCreatedDate(),
        is(equalTo(account.getCreationDetails().getCreatedDate()))
    );
  }
}

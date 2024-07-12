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

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.dto.IntegrationEmailDto;
import org.openlmis.referencedata.testbuilder.IntegrationEmailDataBuilder;

public class IntegrationEmailTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(IntegrationEmail.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    IntegrationEmail integrationEmail = new IntegrationEmailDataBuilder().build();
    ToStringTestUtils.verify(IntegrationEmail.class, integrationEmail, "TEXT");
  }

  @Test
  public void shouldCreateNewInstance() {
    IntegrationEmail integrationEmail = new IntegrationEmailDataBuilder().build();
    IntegrationEmailDto dto = IntegrationEmailDto.newInstance(integrationEmail);

    IntegrationEmail newIntegrationEmail = IntegrationEmail.newInstance(dto);

    assertThat(newIntegrationEmail).isEqualTo(integrationEmail);
  }

  @Test
  public void shouldUpdateFrom() {
    IntegrationEmail integrationEmail = new IntegrationEmailDataBuilder().build();
    IntegrationEmailDto dto = IntegrationEmailDto.newInstance(integrationEmail);
    dto.setEmail("test@email.com");

    integrationEmail.updateFrom(dto);

    assertThat(integrationEmail.getEmail()).isEqualTo("test@email.com");
  }

  @Test
  public void shouldExportData() {
    IntegrationEmail integrationEmail = new IntegrationEmailDataBuilder().build();
    IntegrationEmailDto dto = new IntegrationEmailDto();

    integrationEmail.export(dto);

    assertThat(dto.getId()).isEqualTo(integrationEmail.getId());
    assertThat(dto.getEmail()).isEqualTo(integrationEmail.getEmail());
  }

}

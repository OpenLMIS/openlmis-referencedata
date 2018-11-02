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

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class OrderableIdentityTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(OrderableIdentity.class)
        .verify();
  }

  @Test
  public void shouldInsertDefaultValues() {
    // when
    OrderableIdentity identity = new OrderableIdentity(null, null);

    // then
    assertThat(identity.getId()).isNotNull();
    assertThat(identity.getVersionId()).isEqualTo(1L);
  }

  @Test
  public void shouldUsePassedValues() {
    // given
    UUID id = UUID.randomUUID();
    Long versionId = 100L;

    // when
    OrderableIdentity identity = new OrderableIdentity(id, versionId);

    // then
    assertThat(identity.getId()).isEqualTo(id);
    assertThat(identity.getVersionId()).isEqualTo(versionId);
  }
}

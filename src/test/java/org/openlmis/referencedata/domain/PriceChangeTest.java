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
import org.openlmis.referencedata.dto.PriceChangeDto;
import org.openlmis.referencedata.testbuilder.PriceChangeDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramOrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;

public class PriceChangeTest {

  @Test
  public void equalsContract() {
    ProgramOrderable po1 = new ProgramOrderableDataBuilder().build();
    ProgramOrderable po2 = new ProgramOrderable();

    User u1 = new UserDataBuilder().build();
    User u2 = new User();

    EqualsVerifier
        .forClass(PriceChange.class)
        .withRedefinedSuperclass()
        .withPrefabValues(ProgramOrderable.class, po1, po2)
        .withPrefabValues(User.class, u1, u2)
        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
        .verify();
  }

  @Test
  public void shouldCreateNewInstance() {
    User author = new UserDataBuilder().build();
    PriceChange priceChange = new PriceChangeDataBuilder().withAuthor(author).build();
    PriceChangeDto importer = PriceChangeDto.newInstance(priceChange);

    PriceChange newInstance = PriceChange.newInstance(importer, author);

    assertThat(importer.getPrice()).isEqualTo(newInstance.getPrice());
    assertThat(importer.getAuthor().getId()).isEqualTo(newInstance.getAuthor().getId());
    assertThat(importer.getOccurredDate()).isEqualTo(newInstance.getOccurredDate());
  }

  @Test
  public void shouldExportData() {
    PriceChange priceChange = new PriceChangeDataBuilder().build();
    PriceChangeDto dto = new PriceChangeDto();

    priceChange.export(dto);

    assertThat(dto.getPrice()).isEqualTo(priceChange.getPrice());
    assertThat(dto.getAuthor().getId()).isEqualTo(priceChange.getAuthor().getId());
    assertThat(dto.getOccurredDate()).isEqualTo(priceChange.getOccurredDate());
  }

}

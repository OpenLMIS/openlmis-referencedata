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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.domain.ExtraDataEntity.ExtraDataExporter;

public class ExtraDataEntityTest {
  private static final String KEY = "key";
  private static final String VALUE = "value";

  private ExtraDataEntity entity = new ExtraDataEntity();
  private Map<String, Object> importer = ImmutableMap
      .of(KEY, VALUE);

  @Test
  public void shouldReturnDefaultEntityIfParamIsNull() {
    assertThat(ExtraDataEntity.defaultEntity(null))
        .isInstanceOf(ExtraDataEntity.class);
  }

  @Test
  public void shouldReturnEntityFromParamIfParamIsNotNull() {
    assertThat(ExtraDataEntity.defaultEntity(entity))
        .isEqualTo(entity);
  }

  @Test
  public void shouldUpdateFromOtherMap() {
    entity.updateFrom(importer);
    assertThat(entity.getExtraData())
        .hasSize(1)
        .containsEntry(KEY, VALUE);
  }

  @Test
  public void shouldUpdateFromEmptyMap() {
    entity.updateFrom(importer);
    assertThat(entity.getExtraData()).hasSize(1);

    entity.updateFrom(Maps.newHashMap());
    assertThat(entity.getExtraData()).isEmpty();
  }

  @Test
  public void shouldUpdateFromNull() {
    entity.updateFrom(importer);
    assertThat(entity.getExtraData()).hasSize(1);

    entity.updateFrom(null);
    assertThat(entity.getExtraData()).isEmpty();
  }

  @Test
  public void shouldExportData() {
    ExtraDataExporter exporter = mock(ExtraDataExporter.class);

    entity.updateFrom(importer);
    entity.export(exporter);

    verify(exporter).setExtraData(importer);
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ExtraDataEntity.class)
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(ExtraDataEntity.class, entity);
  }

}

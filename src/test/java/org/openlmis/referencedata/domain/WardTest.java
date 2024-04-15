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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.referencedata.dto.WardDto;
import org.openlmis.referencedata.testbuilder.WardDataBuilder;

public class WardTest {

  @Test
  public void shouldBeEqualByCode() {
    String testCodeString = "test_code";
    Ward ward = new WardDataBuilder().withCode(testCodeString).build();
    Ward wardDupl = new WardDataBuilder().withCode(testCodeString).build();

    assertTrue(ward.equals(wardDupl));
    assertTrue(wardDupl.equals(ward));
  }

  @Test
  public void shouldCreateNewInstance() {
    Ward importerAsDomain = new WardDataBuilder().build();
    WardDto importer = new WardDto();
    importerAsDomain.export(importer);

    Ward newInstance = Ward.newWard(importer);
    assertThat(newInstance).isEqualTo(importerAsDomain);
  }

  @Test
  public void shouldExportData() {
    Ward ward = new WardDataBuilder().build();
    WardDto dto = new WardDto();

    ward.export(dto);

    assertThat(dto.getId()).isEqualTo(ward.getId());
    assertThat(dto.getName()).isEqualTo(ward.getName());
    assertThat(dto.isDisabled()).isEqualTo(ward.isDisabled());
    assertThat(dto.getDescription()).isEqualTo(ward.getDescription());
    assertThat(dto.getCode()).isEqualTo(ward.getCode().toString());
    assertThat(dto.getFacility().getCode()).isEqualTo(ward.getFacility().getCode());
  }

}

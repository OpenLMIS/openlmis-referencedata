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

package org.openlmis.referencedata.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddDefaultFhirFacilityTypeMigrationIntegrationTest
    extends BaseMigrationIntegrationTest {

  @Override
  void insertDataBeforeMigration() {
    // there is a facility type with display order = 1 in migrations
    // that is why we start from 2
    save(TABLE_FACILITY_TYPES, generateFacilityType(2));
    save(TABLE_FACILITY_TYPES, generateFacilityType(3));
    save(TABLE_FACILITY_TYPES, generateFacilityType(4));
    save(TABLE_FACILITY_TYPES, generateFacilityType(5));
    save(TABLE_FACILITY_TYPES, generateFacilityType(6));
  }

  @Override
  String getTargetBeforeTestMigration() {
    return "20181001094512748";
  }

  @Override
  String getTestMigrationTarget() {
    return "20181008113815489";
  }

  @Override
  void verifyDataAfterMigration() {
    List<Map<String, Object>> rows = getRows(TABLE_FACILITY_TYPES);

    assertThat(rows)
        .hasSize(7)
        .extracting("displayOrder")
        .containsExactly(1, 2, 3, 4, 5, 6, 7);
  }

  private Map<String, Object> generateFacilityType(int displayOrder) {
    return ImmutableMap
        .<String, Object>builder()
        .put("id", UUID.randomUUID().toString())
        .put("code", "C" + getNextInstanceNumber())
        .put("active", true)
        .put("displayorder", displayOrder)
        .build();
  }

}

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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;

public class CreateDispensablesMigrationIntegrationTest extends BaseMigrationIntegrationTest {

  @Override
  void insertDataBeforeMigration() {
    save(TABLE_ORDERABLES, generateOrderable());
    save(TABLE_ORDERABLES, generateOrderable());
    save(TABLE_ORDERABLES, generateOrderable());
    save(TABLE_ORDERABLES, generateOrderable());
    save(TABLE_ORDERABLES, generateOrderable());
  }

  @Override
  String getTargetBeforeTestMigration() {
    return "20180129143106317";
  }

  @Override
  String getTestMigrationTarget() {
    return "20180205211153916";
  }

  @Override
  void verifyDataAfterMigration() {
    List<Map<String, Object>> orderables = getRows(TABLE_ORDERABLES);
    List<Map<String, Object>> dispensables = getRows(TABLE_DISPENSABLES);
    List<Map<String, Object>> dispensableAttributes = getRows(TABLE_DISPENSABLE_ATTRIBUTES);

    assertThat(dispensables, hasSize(orderables.size()));

    for (Map<String, Object> orderable : orderables) {
      Map<String, Object> dispensable = dispensables
          .stream()
          .filter(map -> orderable.get("dispensableid").equals(map.get("id")))
          .findFirst()
          .orElse(null);

      assertThat(dispensable, is(notNullValue()));
      assertThat(dispensable.get("type"), is("default"));

      Map<String, Object> attributes = dispensableAttributes
          .stream()
          .filter(map -> dispensable.get("id").equals(map.get("dispensableid")))
          .findFirst()
          .orElse(null);

      assertThat(attributes, is(notNullValue()));
      assertThat(attributes.get("key"), is("dispensingUnit"));
      assertThat(attributes.get("value"), is("10 tab strip"));
    }
  }

  private Map<String, Object> generateOrderable() {
    return ImmutableMap
        .<String, Object>builder()
        .put("id", UUID.randomUUID().toString())
        .put("code", "C" + getNextInstanceNumber())
        .put("description", "Product description goes here.")
        .put("dispensingunit", "10 tab strip")
        .put("fullproductname", RandomStringUtils.randomAlphabetic(10))
        .put("netcontent", 10)
        .put("packroundingthreshold", 1)
        .put("roundtozero", false)
        .build();
  }
}

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
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.List;
import java.util.Map;

@FlywayTarget("20180205211153916")
public class DispensablesMigrationIntegrationTest extends BaseMigrationIntegrationTest {

  @Test
  public void shouldMigrateDispensables() {
    List<Map<String, Object>> orderables = getRows("orderables");
    List<Map<String, Object>> dispensables = getRows("dispensables");
    List<Map<String, Object>> dispensableAttributes = getRows("dispensable_attributes");

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

      // based on data from db/data directory
      // we can't check what value is in this column because the related column in orderables
      // table is removed in migration :(
      assertThat(attributes.get("value"), isOneOf("unit", "10 tab strip", "each"));
    }
  }

}

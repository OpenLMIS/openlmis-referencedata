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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Map;
import java.util.UUID;

/**
 * Tests for orderable's data migrations.
 */
@FlywayTarget("20170517102033421")
public class OrderablesMigrationIntegrationTest extends BaseMigrationIntegrationTest {

  private static final String ID = "'4d1115de-0f60-408a-8a1e-44401e20a5b0'";
  private static final String COMMODITY_TYPE_ID = "'4d1115de-0f60-408a-8a1e-44401e20a5b1'";

  @Test
  public void shouldMigrateOrderables() {
    Map<String, Object> row = getRow("orderables", ID);
    assertThat(row.get("dispensingunit"), is("unit"));
    assertThat(row.get("fullproductname"), is("productname1"));
    assertThat(row.get("packroundingthreshold"), is(10L));
    assertThat(row.get("netcontent"), is(20L));
    assertThat(row.get("code"), is("Code1"));
    assertThat(row.get("roundtozero"), is(false));
  }

  @Test
  public void shouldMigrateTradeItems() {
    Map<String, Object> row = getRow("trade_items", ID);
    assertThat(row.get("manufactureroftradeitem"), is("manufacturer1"));
  }

  @Test
  public void shouldMigrateCommodityTypes() {
    Map<String, Object> row = getRow("commodity_types", COMMODITY_TYPE_ID);
    assertThat(row.get("classificationsystem"), is("cSys"));
    assertThat(row.get("classificationid"), is("cId2"));
    assertThat(row.get("parentid"), is(UUID.fromString("23856848-63c9-4807-9470-603b2ddc33fa")));
  }

}

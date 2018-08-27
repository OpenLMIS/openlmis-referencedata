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

import static java.util.Optional.ofNullable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tests for orderable's data migrations.
 */
public class ReverseRelationshipOfOrderablesMigrationIntegrationTest
    extends BaseMigrationIntegrationTest {
  private static final String ID = "4d1115de-0f60-408a-8a1e-44401e20a5b0";
  private static final String PARENT_ID = "23856848-63c9-4807-9470-603b2ddc33fa";
  private static final String COMMODITY_TYPE_ID = "4d1115de-0f60-408a-8a1e-44401e20a5b1";

  @Override
  void insertDataBeforeMigration() {
    save(TABLE_ORDERABLES, generateOrderable(ID, "TRADE_ITEM", "productname1", "Code1",
        "manufacturer1", null, null));
    save(TABLE_ORDERABLES, generateOrderable(PARENT_ID, "COMMODITY_TYPE", "parentname",
        "CodeParent", null, "cId1", null));
    save(TABLE_ORDERABLES, generateOrderable(COMMODITY_TYPE_ID, "COMMODITY_TYPE", "productname2",
        "Code2", null, "cId2", PARENT_ID));
  }

  @Override
  String getTargetBeforeTestMigration() {
    return "20170516132139856";
  }

  @Override
  String getTestMigrationTarget() {
    return "20170517102033421";
  }

  @Override
  void verifyDataAfterMigration() {
    Map<String, Object> orderable = getRow(TABLE_ORDERABLES, ID);
    assertThat(orderable.get("dispensingunit"), is("unit"));
    assertThat(orderable.get("fullproductname"), is("productname1"));
    assertThat(orderable.get("packroundingthreshold"), is(10L));
    assertThat(orderable.get("netcontent"), is(20L));
    assertThat(orderable.get("code"), is("Code1"));
    assertThat(orderable.get("roundtozero"), is(false));

    Map<String, Object> tradeItem = getRow(TABLE_TRADE_ITEMS, ID);
    assertThat(tradeItem.get("manufactureroftradeitem"), is("manufacturer1"));

    Map<String, Object> commodityType = getRow(TABLE_COMMODITY_TYPES, COMMODITY_TYPE_ID);
    assertThat(commodityType.get("classificationsystem"), is("cSys"));
    assertThat(commodityType.get("classificationid"), is("cId2"));
    assertThat(
        commodityType.get("parentid"),
        is(UUID.fromString("23856848-63c9-4807-9470-603b2ddc33fa"))
    );
  }

  private Map<String, Object> generateOrderable(String id, String type, String name,
                                                String code, String manufacturer,
                                                String classificationId, String parentId) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap
        .<String, Object>builder()
        .put("id", id)
        .put("type", type)
        .put("code", code)
        .put("dispensingunit", "unit")
        .put("fullproductname", name)
        .put("netcontent", 20)
        .put("packroundingthreshold", 10)
        .put("roundtozero", false)
        .put("classificationSystem", "cSys");

    ofNullable(manufacturer).ifPresent(elem -> builder.put("manufactureroftradeitem", elem));
    ofNullable(classificationId).ifPresent(elem -> builder.put("classificationId", elem));
    ofNullable(parentId).ifPresent(elem -> builder.put("parentid", elem));

    return builder.build();
  }
}

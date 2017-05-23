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

package org.openlmis.referencedata.flyway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Tests for orderable's data migrations.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Rollback
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class
    })
@FlywayTest
public class OrderablesIntegrationTest {

  private static final String ID = "'4d1115de-0f60-408a-8a1e-44401e20a5b0'";
  private static final String COMMODITY_TYPE_ID = "'4d1115de-0f60-408a-8a1e-44401e20a5b1'";

  @Autowired
  private JdbcTemplate template;

  @FlywayTest(locationsForMigrate = {"/db/migration"})
  @Test
  public void orderablesShouldMigrate() {
    SqlRowSet sqlRowSet = getSqlRowSet("orderables", ID);
    assertEquals("unit", sqlRowSet.getString("dispensingunit"));
    assertEquals("productname1", sqlRowSet.getString("fullproductname"));
    assertEquals(10, sqlRowSet.getInt("packroundingthreshold"));
    assertEquals(20, sqlRowSet.getInt("netcontent"));
    assertEquals("Code1", sqlRowSet.getString("code"));
    assertFalse(sqlRowSet.getBoolean("roundtozero"));
  }

  @FlywayTest(locationsForMigrate = {"/db/migration"})
  @Test
  public void tradeItemsShouldMigrate() {
    SqlRowSet sqlRowSet = getSqlRowSet("trade_items", ID);
    assertEquals("manufacturer1", sqlRowSet.getString("manufactureroftradeitem"));
  }

  @FlywayTest(locationsForMigrate = {"/db/migration"})
  @Test
  public void commodityTypesShouldMigrate() {
    SqlRowSet sqlRowSet = getSqlRowSet("commodity_types", COMMODITY_TYPE_ID);
    assertEquals("cSys", sqlRowSet.getString("classificationsystem"));
    assertEquals("cId", sqlRowSet.getString("classificationid"));
    assertEquals("23856848-63c9-4807-9470-603b2ddc33fa", sqlRowSet.getString("parentid"));
  }

  private SqlRowSet getSqlRowSet(String object, String id) {
    assertEquals(1, queryForLong("SELECT COUNT(*) from " + object + " where id = " + id));

    SqlRowSet sqlRowSet = template.queryForRowSet("SELECT * FROM " + object + " where id = " + id);
    sqlRowSet.next();
    return sqlRowSet;
  }

  private long queryForLong(String sql) {
    return template.queryForObject(sql, Integer.class).longValue();
  }
}

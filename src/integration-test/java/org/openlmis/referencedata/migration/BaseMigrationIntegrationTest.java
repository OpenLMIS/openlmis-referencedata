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

import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = BaseMigrationIntegrationTest.TestConfig.class)
@Transactional
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    FlywayTargetExecutionListener.class,
    FlywayTestExecutionListener.class})
@FlywayTest(locationsForMigrate = {"/db/data"})
public abstract class BaseMigrationIntegrationTest {
  private static final String SQL_COUNT = "SELECT COUNT(*) from %s";
  private static final String SQL_SELECT = "SELECT * FROM %s";

  private static final String SQL_COUNT_BY_ID = SQL_COUNT + " where id = %s";
  private static final String SQL_SELECT_BY_ID = SQL_SELECT + " where id = %s";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  List<Map<String, Object>> getRows(String table) {
    return executeSelectQuery(SQL_SELECT, table);
  }

  Map<String, Object> getRow(String table, String id) {
    assertThat(executeCountQuery(SQL_COUNT_BY_ID, table, id), is(1L));
    return executeSelectQuery(SQL_SELECT_BY_ID, table, id).get(0);
  }

  private long executeCountQuery(String template, String... params) {
    String sql = createSql(template, params);
    return jdbcTemplate
        .queryForObject(sql, Integer.class)
        .longValue();
  }

  private List<Map<String, Object>> executeSelectQuery(String template, String... params) {
    String sql = createSql(template, params);
    return jdbcTemplate.queryForList(sql);
  }

  private String createSql(String template, String... params) {
    return String.format(ENGLISH, template, (Object[]) params);
  }

  @Configuration
  @EnableConfigurationProperties
  @PropertySource(value = "classpath:application.properties")
  public static class TestConfig {
    // We don't need all beans that are created in Application class.

    /**
     * Creates Flyway instance for migration tests.
     */
    @Bean
    @ConfigurationProperties("flyway")
    public Flyway flyway() {
      Flyway flyway = new Flyway();
      flyway.setDataSource(dataSource());

      return flyway;
    }

    /**
     * Creates data source for migration tests.
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
      return new BasicDataSource();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
      return new JdbcTemplate(dataSource());
    }

  }

}

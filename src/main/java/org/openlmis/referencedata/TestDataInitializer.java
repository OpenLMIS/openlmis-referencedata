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

package org.openlmis.referencedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openlmis.referencedata.util.Resource2Db;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Loads layered test data into an empty database when a data profile is active.
 *
 * <p>Each data profile is a layer stored in {@code db/<layer>/}, with one CSV file per table
 * named {@code <schema>.<table>.csv}. Every data profile implies {@code base-data}, which is
 * loaded first. A layer only holds files for the tables it adds rows to, and it may refer to
 * rows of the layers loaded before it, never after it.
 */
@Component
@Profile("(base-data | demo-data | functional-test-data | edge-case-data | performance-data)"
    + " & !test-run")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  static final String BASE_DATA = "base-data";
  static final String DEMO_DATA = "demo-data";
  static final String FUNCTIONAL_TEST_DATA = "functional-test-data";
  static final String EDGE_CASE_DATA = "edge-case-data";
  static final String PERFORMANCE_DATA = "performance-data";

  /**
   * All data layers, in load order.
   */
  static final List<String> LAYERS = Collections.unmodifiableList(Arrays.asList(
      BASE_DATA, DEMO_DATA, FUNCTIONAL_TEST_DATA, EDGE_CASE_DATA, PERFORMANCE_DATA));

  /**
   * All tables a layer may hold, in an order that satisfies the foreign keys between them.
   */
  static final List<String> TABLES = Collections.unmodifiableList(Arrays.asList(
      "referencedata.geographic_levels",
      "referencedata.geographic_zones",
      "referencedata.facility_operators",
      "referencedata.facility_types",
      "referencedata.facilities",
      "referencedata.supervisory_nodes",
      "referencedata.processing_schedules",
      "referencedata.processing_periods",
      "referencedata.commodity_types",
      "referencedata.orderable_display_categories",
      "referencedata.dispensables",
      "referencedata.dispensable_attributes",
      "referencedata.orderables",
      "referencedata.orderable_identifiers",
      "referencedata.programs",
      "referencedata.program_orderables",
      "referencedata.supply_lines",
      "referencedata.users",
      "referencedata.roles",
      "referencedata.role_rights",
      "referencedata.role_assignments",
      "referencedata.requisition_groups",
      "referencedata.requisition_group_program_schedules",
      "referencedata.requisition_group_members",
      "referencedata.supported_programs",
      "referencedata.trade_items",
      "referencedata.trade_item_classifications",
      "referencedata.ideal_stock_amounts",
      "referencedata.lots",
      "referencedata.service_accounts",
      "referencedata.facility_type_approved_products",
      "referencedata.supply_partners",
      "referencedata.supply_partner_associations",
      "referencedata.supply_partner_association_facilities",
      "referencedata.supply_partner_association_orderables"));

  private final Resource2Db loader;
  private final ResourceLoader resourceLoader;
  private final Environment environment;

  @Autowired
  public TestDataInitializer(JdbcTemplate template, ResourceLoader resourceLoader,
      Environment environment) {
    this(new Resource2Db(template), resourceLoader, environment);
  }

  TestDataInitializer(Resource2Db loader, ResourceLoader resourceLoader,
      Environment environment) {
    this.loader = loader;
    this.resourceLoader = resourceLoader;
    this.environment = environment;
  }

  /**
   * Loads every table file of each active data layer.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    List<String> layers = resolveLayers(environment.getActiveProfiles());
    XLOGGER.info("Loading data layers: " + layers);

    for (String layer : layers) {
      int loaded = 0;
      for (String table : TABLES) {
        Resource resource = resourceLoader.getResource(location(layer, table));
        if (resource.exists()) {
          loader.insertToDbFromCsv(table, resource);
          loaded++;
        }
      }
      XLOGGER.info("Data layer " + layer + ": loaded " + loaded + " table file(s)");
    }

    XLOGGER.exit();
  }

  /**
   * Returns the data layers to load for the given active profiles, in load order. Any data
   * profile implies {@code base-data}.
   *
   * @throws IllegalStateException when both demo-data and functional-test-data are active
   */
  static List<String> resolveLayers(String... activeProfiles) {
    List<String> active = Arrays.asList(activeProfiles);

    if (active.contains(DEMO_DATA) && active.contains(FUNCTIONAL_TEST_DATA)) {
      throw new IllegalStateException("The " + DEMO_DATA + " and " + FUNCTIONAL_TEST_DATA
          + " profiles cannot be active together; choose one of them");
    }

    List<String> layers = new ArrayList<>();
    for (String layer : LAYERS) {
      if (BASE_DATA.equals(layer) || active.contains(layer)) {
        layers.add(layer);
      }
    }
    return layers;
  }

  static String location(String layer, String table) {
    return "classpath:db/" + layer + "/" + table + ".csv";
  }
}

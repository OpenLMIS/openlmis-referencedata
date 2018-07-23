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
import org.openlmis.referencedata.util.Resource2Db;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("performance-data")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);
  private static final String DEMO_DATA_PATH = "classpath:db/demo-data/";

  @Value(value = DEMO_DATA_PATH + "referencedata.users.sql")
  private Resource usersResource;

  @Value(value = DEMO_DATA_PATH + "facilities.csv")
  private Resource facilitiesResource;

  @Value(value = DEMO_DATA_PATH + "orderables.csv")
  private Resource orderablesResource;

  @Value(value = DEMO_DATA_PATH + "requisition_group_members.csv")
  private Resource requisitionGroupMembersResource;

  @Value(value = DEMO_DATA_PATH + "supported_programs.csv")
  private Resource supportedProgramsResource;

  @Value(value = DEMO_DATA_PATH + "full_supply_products.csv")
  private Resource fullSupplyProductsResource;

  @Value(value = DEMO_DATA_PATH + "non-full_supply_products.csv")
  private Resource nonfullSupplyProductsResource;

  @Value(value = DEMO_DATA_PATH
      + "facility_type_approved_products_for_Essential_Medicines___District_Hospital.csv")
  private Resource ftapResource;

  @Value(value = DEMO_DATA_PATH + "processing_periods.csv")
  private Resource processingPeriodsResource;

  @Autowired
  private JdbcTemplate template;

  /**
   * Initializes test data.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    Resource2Db r2db = new Resource2Db(template);

    r2db.updateDbFromSql(usersResource);
    r2db.insertToDbFromCsv("referencedata.facilities", facilitiesResource);
    r2db.insertToDbFromCsv("referencedata.orderables", orderablesResource);
    r2db.insertToDbFromCsv("referencedata.requisition_group_members",
        requisitionGroupMembersResource);
    r2db.insertToDbFromCsv("referencedata.supported_programs", supportedProgramsResource);
    r2db.insertToDbFromCsv("referencedata.program_orderables", fullSupplyProductsResource);
    r2db.insertToDbFromCsv("referencedata.program_orderables", nonfullSupplyProductsResource);
    r2db.insertToDbFromCsv("referencedata.facility_type_approved_products", ftapResource);
    r2db.insertToDbFromCsv("referencedata.processing_periods", processingPeriodsResource);

    XLOGGER.exit();
  }
}
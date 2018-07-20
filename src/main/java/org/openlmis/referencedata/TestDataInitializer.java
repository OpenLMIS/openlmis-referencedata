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
@Profile("demo-data")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);
  private static final String DEMO_DATA_PATH = "classpath:db/demo-data/";

  @Value(value = DEMO_DATA_PATH + "referencedata.geographic_levels.csv")
  private Resource geographicLevelsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.geographic_zones.csv")
  private Resource geographicZonesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.facility_operators.csv")
  private Resource facilityOperatorsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.facility_types.csv")
  private Resource facilityTypesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.facilities.csv")
  private Resource facilitiesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.supervisory_nodes.csv")
  private Resource supervisoryNodesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.processing_schedules.csv")
  private Resource processingSchedulesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.processing_periods.csv")
  private Resource processingPeriodsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.commodity_types.csv")
  private Resource commodityTypesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.orderable_display_categories.csv")
  private Resource orderableDisplayCategoriesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.dispensables.csv")
  private Resource dispensablesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.dispensable_attributes.csv")
  private Resource dispensableAttributesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.orderables.csv")
  private Resource orderablesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.programs.csv")
  private Resource programsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.program_orderables.csv")
  private Resource programOrderablesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.supply_lines.csv")
  private Resource supplyLinesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.users.csv")
  private Resource usersResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.roles.csv")
  private Resource rolesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.role_rights.csv")
  private Resource roleRightsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.role_assignments.csv")
  private Resource roleAssignmentsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.requisition_groups.csv")
  private Resource requisitionGroupsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.requisition_group_program_schedules.csv")
  private Resource requisitionGroupProgramSchedulesResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.requisition_group_members.csv")
  private Resource requisitionGroupMembersResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.supported_programs.csv")
  private Resource supportedProgramsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.trade_items.csv")
  private Resource tradeItemsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.trade_item_classifications.csv")
  private Resource tradeItemClassificationsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.ideal_stock_amounts.csv")
  private Resource idealStockAmountsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.lots.csv")
  private Resource lotsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.orderable_identifiers.csv")
  private Resource orderableIdentifiersResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.service_accounts.csv")
  private Resource serviceAccountsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.facility_type_approved_products.csv")
  private Resource facilityTypeApprovedProductsResource;

  @Value(value = DEMO_DATA_PATH + "referencedata.users.sql")
  private Resource moreUsersResource;

  @Value(value = DEMO_DATA_PATH + "facilities.csv")
  private Resource moreFacilitiesResource;

  @Value(value = DEMO_DATA_PATH + "orderables.csv")
  private Resource moreOrderablesResource;

  @Value(value = DEMO_DATA_PATH + "requisition_group_members.csv")
  private Resource moreRequisitionGroupMembersResource;

  @Value(value = DEMO_DATA_PATH + "supported_programs.csv")
  private Resource moreSupportedProgramsResource;

  @Value(value = DEMO_DATA_PATH + "full_supply_products.csv")
  private Resource fullSupplyProductsResource;

  @Value(value = DEMO_DATA_PATH + "non-full_supply_products.csv")
  private Resource nonfullSupplyProductsResource;

  @Value(value = DEMO_DATA_PATH
      + "facility_type_approved_products_for_Essential_Medicines___District_Hospital.csv")
  private Resource ftapResource;

  @Autowired
  private JdbcTemplate template;

  /**
   * Initializes test data.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    Resource2Db r2db = new Resource2Db(template);

    r2db.insertToDbFromCsv("referencedata.geographic_levels", geographicLevelsResource);
    r2db.insertToDbFromCsv("referencedata.geographic_zones", geographicZonesResource);
    r2db.insertToDbFromCsv("referencedata.facility_operators", facilityOperatorsResource);
    r2db.insertToDbFromCsv("referencedata.facility_types", facilityTypesResource);
    r2db.insertToDbFromCsv("referencedata.facilities", facilitiesResource);
    r2db.insertToDbFromCsv("referencedata.supervisory_nodes", supervisoryNodesResource);
    r2db.insertToDbFromCsv("referencedata.processing_schedules", processingSchedulesResource);
    r2db.insertToDbFromCsv("referencedata.processing_periods", processingPeriodsResource);
    r2db.insertToDbFromCsv("referencedata.commodity_types", commodityTypesResource);
    r2db.insertToDbFromCsv("referencedata.orderable_display_categories",
        orderableDisplayCategoriesResource);
    r2db.insertToDbFromCsv("referencedata.dispensables", dispensablesResource);
    r2db.insertToDbFromCsv("referencedata.dispensable_attributes", dispensableAttributesResource);
    r2db.insertToDbFromCsv("referencedata.orderables", orderablesResource);
    r2db.insertToDbFromCsv("referencedata.orderable_identifiers", orderableIdentifiersResource);
    r2db.insertToDbFromCsv("referencedata.programs", programsResource);
    r2db.insertToDbFromCsv("referencedata.program_orderables", programOrderablesResource);
    r2db.insertToDbFromCsv("referencedata.supply_lines", supplyLinesResource);
    r2db.insertToDbFromCsv("referencedata.users", usersResource);
    r2db.insertToDbFromCsv("referencedata.roles", rolesResource);
    r2db.insertToDbFromCsv("referencedata.role_rights", roleRightsResource);
    r2db.insertToDbFromCsv("referencedata.role_assignments", roleAssignmentsResource);
    r2db.insertToDbFromCsv("referencedata.requisition_groups", requisitionGroupsResource);
    r2db.insertToDbFromCsv("referencedata.requisition_group_program_schedules",
        requisitionGroupProgramSchedulesResource);
    r2db.insertToDbFromCsv("referencedata.requisition_group_members",
        requisitionGroupMembersResource);
    r2db.insertToDbFromCsv("referencedata.supported_programs", supportedProgramsResource);
    r2db.insertToDbFromCsv("referencedata.trade_items", tradeItemsResource);
    r2db.insertToDbFromCsv("referencedata.trade_item_classifications",
        tradeItemClassificationsResource);
    r2db.insertToDbFromCsv("referencedata.ideal_stock_amounts", idealStockAmountsResource);
    r2db.insertToDbFromCsv("referencedata.lots", lotsResource);
    r2db.insertToDbFromCsv("referencedata.service_accounts", serviceAccountsResource);
    r2db.insertToDbFromCsv("referencedata.facility_type_approved_products",
        facilityTypeApprovedProductsResource);

    // original performance data set
    r2db.updateDbFromSql(moreUsersResource);
    r2db.insertToDbFromCsv("referencedata.facilities", moreFacilitiesResource);
    r2db.insertToDbFromCsv("referencedata.orderables", moreOrderablesResource);
    r2db.insertToDbFromCsv("referencedata.requisition_group_members",
        moreRequisitionGroupMembersResource);
    r2db.insertToDbFromCsv("referencedata.supported_programs", moreSupportedProgramsResource);
    r2db.insertToDbFromCsv("referencedata.program_orderables", fullSupplyProductsResource);
    r2db.insertToDbFromCsv("referencedata.program_orderables", nonfullSupplyProductsResource);
    r2db.insertToDbFromCsv("referencedata.facility_type_approved_products", ftapResource);

    XLOGGER.exit();
  }
}
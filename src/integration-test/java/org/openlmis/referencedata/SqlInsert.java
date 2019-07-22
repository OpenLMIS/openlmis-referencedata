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

import org.apache.commons.lang.StringUtils;

final class SqlInsert {

  static final String NAME = "name";
  static final String CODE = "code";
  static final String DESCRIPTION = "description";
  private static final String DISPLAY_ORDER = "displayorder";
  private static final String ACTIVE = "active";
  private static final String PROGRAM_ID = "programid";
  private static final String PARENT_ID = "parentid";
  
  private static final String[] COMMODITY_TYPES_FIELDS = {
      "id", NAME, "classificationsystem", "classificationid", PARENT_ID
  };

  private static final String[] FACILITY_OPERATORS_FIELDS = {
      "id", CODE, DESCRIPTION, DISPLAY_ORDER, NAME
  };

  private static final String[] FACILITY_FIELDS = {
      "id", ACTIVE, CODE, "comment", DESCRIPTION, "enabled", "godowndate",
      "golivedate", NAME, "openlmisaccessible", "geographiczoneid", "operatedbyid",
      "typeid"
  };

  private static final String[] FTAP_FIELDS = {
      "id", "versionNumber", "emergencyorderpoint", "maxperiodsofstock", "minperiodsofstock",
      "facilitytypeid", "orderableid", PROGRAM_ID
  };

  private static final String[] FACILITY_TYPE_FIELDS = {
      "id", ACTIVE, CODE, DESCRIPTION, DISPLAY_ORDER, NAME
  };

  private static final String[] GEOGRAPHIC_LEVELS_FIELDS = {
      "id", CODE, "levelnumber", NAME
  };

  private static final String[] GEOGRAPHIC_ZONE_FIELDS = {
      "id", CODE, NAME, "levelid", PARENT_ID, "catchmentpopulation",
      "latitude", "longitude"
  };

  private static final String[] LOT_FIELDS = {
      "id", "lotcode", "expirationdate", "manufacturedate", "tradeitemid", ACTIVE
  };

  private static final String[] ORDERABLE_DISPLAY_CATEGORY_FIELDS = {
      "id", CODE, "displayname", DISPLAY_ORDER
  };

  private static final String[] ORDERABLE_FIELDS = {
      "id", "fullproductname", "packroundingthreshold", "netcontent", CODE,
      "roundtozero", DESCRIPTION, "dispensableid", "versionNumber", "lastupdated"
  };

  private static final String[] PROCESSING_PERIOD_FIELDS = {
      "id", DESCRIPTION, "enddate", NAME, "startdate",
      "processingscheduleid"
  };

  private static final String[] PROCESSING_SCHEDULE_FIELDS = {
      "id", CODE, DESCRIPTION, "modifieddate", NAME
  };

  private static final String[] PROGRAM_FIELDS = {
      "id", ACTIVE, CODE, DESCRIPTION, NAME, "periodsskippable",
      "shownonfullsupplytab", "enabledatephysicalstockcountcompleted",
      "skipauthorization"
  };

  private static final String[] REQUISITION_GROUP_PROGRAM_SCHEDULE_FIELDS = {
      "id", "directdelivery", "dropofffacilityid", "processingscheduleid",
      PROGRAM_ID, "requisitiongroupid"
  };

  private static final String[] REQUISITION_GROUPS_FIELDS = {
      "id", CODE, DESCRIPTION, NAME, "supervisorynodeid"
  };

  private static final String[] RIGHT_FIELDS = {
      "id", DESCRIPTION, NAME, "type"
  };

  private static final String[] ROLE_FIELDS = {
      "id", DESCRIPTION, NAME
  };

  private static final String[] SERVICE_ACCOUNT_FIELDS = {
      "id", "createdby", "createddate"
  };

  private static final String[] STOCK_ADJUSTMENT_REASON_FIELDS = {
      "id", "additive", DESCRIPTION, "displayorder", NAME, PROGRAM_ID
  };

  private static final String[] SUPERVISORY_NODES_FIELDS = {
      "id", CODE, DESCRIPTION, NAME, "facilityid", PARENT_ID
  };

  private static final String[] SUPPLY_LINES_FIELDS = {
      "id", DESCRIPTION, PROGRAM_ID, "supervisorynodeid", "supplyingfacilityid"
  };

  private static final String[] TRADE_ITEMS_FIELDS = {
      "id", "manufactureroftradeitem", "gtin"
  };

  private static final String[] USER_FIELDS = {
      "id", "active", "allownotify", "email", "firstname", "lastname",
      "timezone", "username", "verified", "homefacilityid",
      "jobtitle", "phonenumber"
  };

  private static final String[] SUPPLY_PARTNER_FIELDS = {
      "id", "name", "code"
  };

  static final String INSERT_COMMODITY_TYPE_SQL = String.format(
      "INSERT INTO referencedata.commodity_types (%s) VALUES (%s)",
      StringUtils.join(COMMODITY_TYPES_FIELDS, ", "),
      StringUtils.repeat("?", ",", COMMODITY_TYPES_FIELDS.length)
  );

  static final String INSERT_FACILITY_OPERATORS_SQL = String.format(
      "INSERT INTO referencedata.facility_operators (%s) VALUES (%s)",
      StringUtils.join(FACILITY_OPERATORS_FIELDS, ", "),
      StringUtils.repeat("?", ",", FACILITY_OPERATORS_FIELDS.length)
  );

  static final String INSERT_FACILITY_SQL = String.format(
      "INSERT INTO referencedata.facilities (%s) VALUES (%s)",
      StringUtils.join(FACILITY_FIELDS, ", "),
      StringUtils.repeat("?", ",", FACILITY_FIELDS.length)
  );

  static final String INSERT_FTAP_SQL = String.format(
      "INSERT INTO referencedata.facility_type_approved_products (%s) VALUES (%s)",
      StringUtils.join(FTAP_FIELDS, ", "),
      StringUtils.repeat("?", ",", FTAP_FIELDS.length)
  );

  static final String INSERT_FACILITY_TYPE_SQL = String.format(
      "INSERT INTO referencedata.facility_types (%s) VALUES (%s)",
      StringUtils.join(FACILITY_TYPE_FIELDS, ", "),
      StringUtils.repeat("?", ",", FACILITY_TYPE_FIELDS.length)
  );

  static final String INSERT_GEOGRAPHIC_LEVEL_SQL = String.format(
      "INSERT INTO referencedata.geographic_levels (%s) VALUES (%s)",
      StringUtils.join(GEOGRAPHIC_LEVELS_FIELDS, ", "),
      StringUtils.repeat("?", ",", GEOGRAPHIC_LEVELS_FIELDS.length)
  );

  static final String INSERT_GEOGRAPHIC_ZONE_SQL = String.format(
      "INSERT INTO referencedata.geographic_zones (%s) VALUES (%s)",
      StringUtils.join(GEOGRAPHIC_ZONE_FIELDS, ", "),
      StringUtils.repeat("?", ",", GEOGRAPHIC_ZONE_FIELDS.length)
  );

  static final String INSERT_LOT_SQL = String.format(
      "INSERT INTO referencedata.lots (%s) VALUES (%s)",
      StringUtils.join(LOT_FIELDS, ", "),
      StringUtils.repeat("?", ",", LOT_FIELDS.length)
  );

  static final String INSERT_ORDERABLE_DISPLAY_CATEGORY_SQL = String.format(
      "INSERT INTO referencedata.orderable_display_categories (%s) VALUES (%s)",
      StringUtils.join(ORDERABLE_DISPLAY_CATEGORY_FIELDS, ", "),
      StringUtils.repeat("?", ",", ORDERABLE_DISPLAY_CATEGORY_FIELDS.length)
  );

  static final String INSERT_ORDERABLE_SQL = String.format(
      "INSERT INTO referencedata.orderables (%s) VALUES (%s)",
      StringUtils.join(ORDERABLE_FIELDS, ", "),
      StringUtils.repeat("?", ",", ORDERABLE_FIELDS.length)
  );

  static final String INSERT_PROCESSING_PERIOD_SQL = String.format(
      "INSERT INTO referencedata.processing_periods (%s) VALUES (%s)",
      StringUtils.join(PROCESSING_PERIOD_FIELDS, ", "),
      StringUtils.repeat("?", ",", PROCESSING_PERIOD_FIELDS.length)
  );

  static final String INSERT_PROCESSING_SCHEDULE_SQL = String.format(
      "INSERT INTO referencedata.processing_schedules (%s) VALUES (%s)",
      StringUtils.join(PROCESSING_SCHEDULE_FIELDS, ", "),
      StringUtils.repeat("?", ",", PROCESSING_SCHEDULE_FIELDS.length)
  );

  static final String INSERT_PROGRAM_SQL = String.format(
      "INSERT INTO referencedata.programs (%s) VALUES (%s)",
      StringUtils.join(PROGRAM_FIELDS, ", "),
      StringUtils.repeat("?", ",", PROGRAM_FIELDS.length)
  );

  static final String INSERT_REQUISITION_GROUP_PROGRAM_SCHEDULE_SQL = String.format(
      "INSERT INTO referencedata.requisition_group_program_schedules (%s) VALUES (%s)",
      StringUtils.join(REQUISITION_GROUP_PROGRAM_SCHEDULE_FIELDS, ", "),
      StringUtils.repeat("?", ",", REQUISITION_GROUP_PROGRAM_SCHEDULE_FIELDS.length)
  );

  static final String INSERT_REQUISITION_GROUP_SQL = String.format(
      "INSERT INTO referencedata.requisition_groups (%s) VALUES (%s)",
      StringUtils.join(REQUISITION_GROUPS_FIELDS, ", "),
      StringUtils.repeat("?", ",", REQUISITION_GROUPS_FIELDS.length)
  );

  static final String INSERT_RIGHT_SQL = String.format(
      "INSERT INTO referencedata.rights (%s) VALUES (%s)",
      StringUtils.join(RIGHT_FIELDS, ", "),
      StringUtils.repeat("?", ",", RIGHT_FIELDS.length)
  );

  static final String INSERT_ROLE_SQL = String.format(
      "INSERT INTO referencedata.roles (%s) VALUES (%s)",
      StringUtils.join(ROLE_FIELDS, ", "),
      StringUtils.repeat("?", ",", ROLE_FIELDS.length)
  );

  static final String INSERT_SERVICE_ACCOUNT_SQL = String.format(
      "INSERT INTO referencedata.service_accounts (%s) VALUES (%s)",
      StringUtils.join(SERVICE_ACCOUNT_FIELDS, ", "),
      StringUtils.repeat("?", ",", SERVICE_ACCOUNT_FIELDS.length)
  );

  static final String INSERT_STOCK_ADJUSTMENT_REASON_SQL = String.format(
      "INSERT INTO referencedata.stock_adjustment_reasons (%s) VALUES (%s)",
      StringUtils.join(STOCK_ADJUSTMENT_REASON_FIELDS, ", "),
      StringUtils.repeat("?", ",", STOCK_ADJUSTMENT_REASON_FIELDS.length)
  );

  static final String INSERT_SUPERVISORY_NODE_SQL = String.format(
      "INSERT INTO referencedata.supervisory_nodes (%s) VALUES (%s)",
      StringUtils.join(SUPERVISORY_NODES_FIELDS, ", "),
      StringUtils.repeat("?", ",", SUPERVISORY_NODES_FIELDS.length)
  );

  static final String INSERT_SUPPLY_LINE_SQL = String.format(
      "INSERT INTO referencedata.supply_lines (%s) VALUES (%s)",
      StringUtils.join(SUPPLY_LINES_FIELDS, ", "),
      StringUtils.repeat("?", ",", SUPPLY_LINES_FIELDS.length)
  );

  static final String INSERT_TRADE_ITEM_SQL = String.format(
      "INSERT INTO referencedata.trade_items (%s) VALUES (%s)",
      StringUtils.join(TRADE_ITEMS_FIELDS, ", "),
      StringUtils.repeat("?", ",", TRADE_ITEMS_FIELDS.length)
  );

  static final String INSERT_USER_SQL = String.format(
      "INSERT INTO referencedata.users (%s) VALUES (%s)",
      StringUtils.join(USER_FIELDS, ", "),
      StringUtils.repeat("?", ",", USER_FIELDS.length)
  );

  static final String INSERT_SUPPLY_PARTNER_SQL = String.format(
      "INSERT INTO referencedata.supply_partners (%s) VALUES (%s)",
      StringUtils.join(SUPPLY_PARTNER_FIELDS, ", "),
      StringUtils.repeat("?", ",", SUPPLY_PARTNER_FIELDS.length)
  );

  private SqlInsert() {
    throw new UnsupportedOperationException();
  }
}

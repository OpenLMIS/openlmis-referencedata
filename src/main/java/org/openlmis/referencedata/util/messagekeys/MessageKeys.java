package org.openlmis.referencedata.util.messagekeys;

import java.util.Arrays;

public abstract class MessageKeys {
  private static final String DELIMITER = ".";

  // General
  private static final String SERVICE = "requisition";
  protected static final String SERVICE_ERROR = join(SERVICE, "error");

  protected static final String NON_EXISTENT = "nonExistent";
  protected static final String UNAUTHORIZED = "unauthorized";
  protected static final String DUPLICATED = "duplicated";
  protected static final String VALIDATION = "validation";
  protected static final String NOT_FOUND = "notFound";
  protected static final String REQUIRED = "required";
  protected static final String EXTERNAL = "external";
  protected static final String DELETING = "deleting";
  protected static final String CREATED = "created";
  protected static final String GENERIC = "generic";
  protected static final String WITHOUT = "without";
  protected static final String SEARCH = "search";
  protected static final String FAILED = "failed";
  protected static final String SAVING = "saving";
  protected static final String EMPTY = "empty";
  protected static final String RESET = "reset";
  protected static final String WRONG = "wrong";
  protected static final String WITH = "with";
  protected static final String NULL = "null";
  protected static final String NAME = "name";
  protected static final String CODE = "code";
  protected static final String AND = "and";
  protected static final String ID = "id";

  // Entities
  protected static final String USER = "user";
  protected static final String ROLE = "role";
  protected static final String RIGHT = "right";
  protected static final String PROGRAM = "program";
  protected static final String PRODUCT = "product";
  protected static final String FACILITY = "facility";
  protected static final String SUPPLY_LINE = "supplyLine";
  protected static final String FACILITY_TYPE = "facilityType";
  protected static final String GEOGRAPHIC_ZONE = "geographicZone";
  protected static final String GEOGRAPHIC_LEVEL = "geographicLevel";
  protected static final String SUPERVISORY_NODE = "supervisoryNode";
  protected static final String PRODUCT_CATEGORY = "productCategory";
  protected static final String ORDERABLE_PRODUCT = "orderableProduct";
  protected static final String FACILITY_OPERATOR = "facilityOperator";
  protected static final String PROCESSING_PERIOD = "processingPeriod";
  protected static final String PROCESSING_SCHEDULE = "processingSchedule";
  protected static final String ORDERED_DISPLAY_VALUE = "orderedDisplayValue";
  protected static final String STOCK_ADJUSTMENT_REASON = "stockAdjustmentReason";
  protected static final String FACILITY_TYPE_APPROVED_PRODUCT = "facilityTypeApprovedProduct";

  // Common to subclasses
  protected static final String EMAIL = "email";
  protected static final String BODY = "body";
  protected static final String ACCOUNT = "account";
  protected static final String SUBJECT = "subject";
  protected static final String PASSWORD = "password";
  protected static final String PROGRAM_REPOSITORY = "programRepository";
  protected static final String PRODUCT_CATEGORY_REPOSITORY = "productCategoryRepository";

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

  protected MessageKeys() {
    throw new UnsupportedOperationException();
  }
}

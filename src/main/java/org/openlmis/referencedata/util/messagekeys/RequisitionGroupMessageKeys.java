package org.openlmis.referencedata.util.messagekeys;

public abstract class RequisitionGroupMessageKeys extends MessageKeys {
  private static final String REQUISITION_GROUP = "requisitionGroup";
  private static final String ERROR = join(SERVICE_ERROR, REQUISITION_GROUP);
  private static final String TOO_LONG = "tooLong";
  private static final String DESCRIPTION = "description";

  public static final String ERROR_NULL = join(ERROR, NULL);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);

  public static final String ERROR_DESCRIPTION_TOO_LONG = join(ERROR, DESCRIPTION, TOO_LONG);

  public static final String ERROR_CODE_REQUIRED = join(ERROR, CODE, REQUIRED);
  public static final String ERROR_CODE_DUPLICATED = join(ERROR, CODE, DUPLICATED);
  public static final String ERROR_CODE_TOO_LONG = join(ERROR, CODE, TOO_LONG);

  public static final String ERROR_NAME_TOO_LONG = join(ERROR, NAME, TOO_LONG);
  public static final String ERROR_NAME_REQUIRED = join(ERROR, NAME, REQUIRED);

  public static final String ERROR_FACILITY_NULL = join(ERROR, FACILITY, NULL);
  public static final String ERROR_FACILITY_ID_REQUIRED = join(ERROR, FACILITY, REQUIRED);
  public static final String ERROR_FACILITY_NON_EXISTENT = join(ERROR, FACILITY, NON_EXISTENT);

  public static final String ERROR_SUPERVISORY_NODE_REQUIRED =
      join(ERROR, SUPERVISORY_NODE, REQUIRED);
  public static final String ERROR_SUPERVISORY_NODE_ID_REQUIRED =
      join(ERROR, SUPERVISORY_NODE, REQUIRED);
  public static final String ERROR_SUPERVISORY_NODE_NON_EXISTENT =
      join(ERROR, SUPERVISORY_NODE, NON_EXISTENT);
}

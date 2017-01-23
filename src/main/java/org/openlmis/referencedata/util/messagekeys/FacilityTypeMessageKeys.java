package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityTypeMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY_TYPE);

  public static final String ERROR_SAVING_WITH_ID = join(ERROR, SAVING, WITH, ID);
  public static final String ERROR_DELETING_WITH_ID = join(ERROR, DELETING, WITH, ID);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

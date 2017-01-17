package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityTypeMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY_TYPE);

  public static final String ERROR_SAVING = join(ERROR, SAVING);
  public static final String ERROR_DELETING = join(ERROR, DELETING);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

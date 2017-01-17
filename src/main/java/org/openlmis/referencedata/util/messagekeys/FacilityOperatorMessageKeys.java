package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityOperatorMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY_OPERATOR);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

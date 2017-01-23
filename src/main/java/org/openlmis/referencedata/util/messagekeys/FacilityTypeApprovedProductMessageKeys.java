package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityTypeApprovedProductMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY_TYPE_APPROVED_PRODUCT);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

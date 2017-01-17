package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY);
  public static final String FACILITY_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String FACILITY_NOT_FOUND_WITH_ID = join(FACILITY_NOT_FOUND, WITH, ID);
}

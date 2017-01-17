package org.openlmis.referencedata.util.messagekeys;

public abstract class FacilityMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, FACILITY);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
  public static final String ERROR_SEARCH_CODE_NULL_AND_NAME_NULL =
      join(ERROR, SEARCH, CODE, NULL, AND, NAME, NULL);
}

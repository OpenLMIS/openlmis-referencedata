package org.openlmis.referencedata.util.messagekeys;

public abstract class RightMessageKeys  extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, RIGHT);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
  public static final String ERROR_NAME_NON_EXISTENT = join(ERROR, NAME, NON_EXISTENT);
}

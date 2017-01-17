package org.openlmis.referencedata.util.messagekeys;

public abstract class ProgramMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, PROGRAM);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NON_EXISTENT = join(ERROR, NON_EXISTENT);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
}

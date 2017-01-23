package org.openlmis.referencedata.util.messagekeys;

public abstract class RoleMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, ROLE);

  public static final String ERROR_DUPLICATED = join(ERROR, DUPLICATED);
}

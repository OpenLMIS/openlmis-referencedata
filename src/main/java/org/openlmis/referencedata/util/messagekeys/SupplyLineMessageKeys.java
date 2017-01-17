package org.openlmis.referencedata.util.messagekeys;

public abstract class SupplyLineMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, SUPPLY_LINE);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

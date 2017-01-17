package org.openlmis.referencedata.util.messagekeys;

public abstract class ProductMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, PRODUCT);
  private static final String DISPENSING_UNITS = "dispensingUnits";

  public static final String ERROR_DISPENSING_UNITS_WRONG = join(ERROR, DISPENSING_UNITS, WRONG);
}

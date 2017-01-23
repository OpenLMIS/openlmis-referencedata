package org.openlmis.referencedata.util.messagekeys;

public abstract class StockAdjustmentReasonMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, STOCK_ADJUSTMENT_REASON);

  public static final String ERROR_ID_NULL = join(ERROR, ID, NULL);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
}

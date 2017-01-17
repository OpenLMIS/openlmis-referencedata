package org.openlmis.referencedata.util.messagekeys;

public abstract class StockAdjustmentReasonMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, STOCK_ADJUSTMENT_REASON);

  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR, NOT_FOUND, WITH, ID);
}

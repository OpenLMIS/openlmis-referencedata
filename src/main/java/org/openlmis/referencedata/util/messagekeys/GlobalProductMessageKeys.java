package org.openlmis.referencedata.util.messagekeys;

public abstract class GlobalProductMessageKeys extends MessageKeys {
  private static final String ERROR = join(GLOBAL_PRODUCT, SERVICE_ERROR);
  private static final String TRADE_ITEMS = "tradeItems";

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_TRADE_ITEMS_NULL = join(ERROR, TRADE_ITEMS, NULL);
}

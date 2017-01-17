package org.openlmis.referencedata.util.messagekeys;

public abstract class OrderableProductMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, ORDERABLE_PRODUCT);

  public static final String NOT_FOUND = join(ERROR, MessageKeys.NOT_FOUND);
}

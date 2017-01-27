package org.openlmis.referencedata.util.messagekeys;

public abstract class OrderableMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, ORDERABLE);

  public static final String NOT_FOUND = join(ERROR, MessageKeys.NOT_FOUND);
}

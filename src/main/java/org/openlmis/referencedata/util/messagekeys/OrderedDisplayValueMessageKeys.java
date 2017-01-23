package org.openlmis.referencedata.util.messagekeys;

public abstract class OrderedDisplayValueMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, ORDERED_DISPLAY_VALUE);
  private static final String DISPLAY_NAME = "displayName";

  public static final String ERROR_DISPLAY_NAME_EMPTY = join(ERROR, DISPLAY_NAME, EMPTY);
}

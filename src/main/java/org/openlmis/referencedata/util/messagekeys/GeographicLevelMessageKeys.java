package org.openlmis.referencedata.util.messagekeys;

public abstract class GeographicLevelMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, GEOGRAPHIC_LEVEL);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

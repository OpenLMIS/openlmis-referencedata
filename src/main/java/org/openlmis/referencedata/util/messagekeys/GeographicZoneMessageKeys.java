package org.openlmis.referencedata.util.messagekeys;

public abstract class GeographicZoneMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, GEOGRAPHIC_ZONE);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

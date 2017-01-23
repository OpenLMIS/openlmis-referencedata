package org.openlmis.referencedata.util.messagekeys;

public abstract class ProcessingScheduleMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, PROCESSING_SCHEDULE);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
}

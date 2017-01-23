package org.openlmis.referencedata.util.messagekeys;

public abstract class ValidationMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, VALIDATION);
  private static final String CONTEXTUAL_STATE = "contextualState";

  public static final String ERROR_CONTEXTUAL_STATE_NULL = join(ERROR, CONTEXTUAL_STATE, NULL);
}

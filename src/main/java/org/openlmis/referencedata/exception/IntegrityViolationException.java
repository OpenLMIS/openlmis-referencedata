package org.openlmis.referencedata.exception;

public class IntegrityViolationException extends BaseMessageException {

  public IntegrityViolationException(String messageKey, Throwable cause) {
    super(messageKey, cause);
  }
}

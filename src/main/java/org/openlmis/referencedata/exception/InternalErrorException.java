package org.openlmis.referencedata.exception;

public class InternalErrorException extends BaseMessageException {

  public InternalErrorException(String messageKey, Throwable cause) {
    super(messageKey, cause);
  }
}

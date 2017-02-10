package org.openlmis.referencedata.exception;

public class ExternalApiException extends BaseMessageException {

  public ExternalApiException(String messageKey, Throwable cause) {
    super(messageKey, cause);
  }
}

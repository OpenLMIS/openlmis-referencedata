package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class IntegrityViolationException extends BaseMessageException {

  public IntegrityViolationException(Message message, Throwable cause) {
    super(message, cause);
  }

  public IntegrityViolationException(String messageKey, Throwable cause) {
    super(messageKey, cause);
  }
}

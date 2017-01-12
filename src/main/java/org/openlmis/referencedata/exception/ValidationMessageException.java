package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class ValidationMessageException extends BaseMessageException {

  /**
   * Create new validation exception with the given message key.  Helper method that
   * uses {@link #ValidationMessageException(Message)}.
   * @param messageKey the messageKey of a {@link Message}.
   */
  public ValidationMessageException(String messageKey) {
    super( messageKey );
  }

  /**
   * Create a new validation exception with the given message.
   * @param message the message.
   */
  public ValidationMessageException(Message message) {
    super(message);
  }
}

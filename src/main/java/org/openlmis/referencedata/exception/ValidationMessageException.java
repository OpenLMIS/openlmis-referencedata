package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class ValidationMessageException extends RuntimeException {

  private final Message message;

  /**
   * Create new validation exception with the given message key.  Helper method that
   * uses {@link #ValidationMessageException(Message)}.
   * @param messageKey the messageKey of a {@link Message}.
   */
  public ValidationMessageException(String messageKey) {
    this( new Message(messageKey) );
  }

  /**
   * Create a new validation exception with the given message.
   * @param message the message.
   */
  public ValidationMessageException(Message message) {
    super(message.toString());
    this.message = message;
  }

  public Message asMessage() {
    return message;
  }

  /**
   * Overrides RuntimeException's public String getMessage().
   *
   * @return a localized string description
   */
  @Override
  public String getMessage() {
    return this.message.toString();
  }
}

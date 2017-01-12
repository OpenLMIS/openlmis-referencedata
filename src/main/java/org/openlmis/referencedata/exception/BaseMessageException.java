package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;


/**
 * Base class for building {@link RuntimeException} with {@link Message}.  This should be
 * extended and the constructor's used.  This class should not be caught nor should it be handled
 * through any Spring handler.  Only sub-classes should have handlers.
 */
public abstract class BaseMessageException extends RuntimeException {
  private Message message;

  /**
   * Create new exception with the given message key.  Helper method that
   * uses {@link #BaseMessageException(Message)}.
   * @param messageKey the messageKey of a {@link Message}.
   */
  protected BaseMessageException(String messageKey) {
    this( new Message(messageKey) );
  }

  /**
   * Create a new exception with the given message.
   * @param message the message.
   */
  protected BaseMessageException(Message message) {
    super(message.toString());
    this.message = message;
  }

  /**
   * Get the {@link Message} contained in this exception.
   * @return the message
   */
  public final Message asMessage() {
    return message;
  }

  /**
   * Overrides {@link RuntimeException#getMessage()}.
   *
   * @return a localized string description
   */
  @Override
  public String getMessage() {
    return this.message.toString();
  }
}

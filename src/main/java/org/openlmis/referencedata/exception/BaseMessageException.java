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
    this(new Message(messageKey));
  }

  /**
   * Create new exception with the given message key.  Helper method that
   * uses {@link #BaseMessageException(Message, Throwable)}.
   * @param messageKey the messageKey of a {@link Message}.
   * @param cause the cause of this exception.
   */
  protected BaseMessageException(String messageKey, Throwable cause) {
    this(new Message(messageKey), cause);
  }

  /**
   * Create a new exception with the given message.
   * @param message the message.
   */
  protected BaseMessageException(Message message) {
    this(message, null);
  }

  /**
   * Create a new exception with the given message and cause.
   * @param message the message.
   * @param cause the cause of this exception.
   */
  protected BaseMessageException(Message message, Throwable cause) {
    super(message.toString(), cause);
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

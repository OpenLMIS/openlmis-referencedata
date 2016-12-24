package org.openlmis.referencedata.exception;

import org.apache.commons.lang3.Validate;
import org.openlmis.referencedata.util.Message;

public class AuthException extends RuntimeException {
  private Message message;

  public AuthException(String message) {
    this(new Message( message ));

  }

  /**
   * Create a new auth exception from a Message.
   * @param message the message of this exception.
   */
  public AuthException(Message message) {
    super( message.toString() );
    Validate.notNull(message);
    this.message = message;
  }
}

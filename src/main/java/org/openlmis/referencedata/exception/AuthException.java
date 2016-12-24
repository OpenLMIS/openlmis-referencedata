package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class AuthException extends RuntimeException {
  private Message message;

  public AuthException(String message) {
    super(message);
    this.message = new Message( message );
  }
}

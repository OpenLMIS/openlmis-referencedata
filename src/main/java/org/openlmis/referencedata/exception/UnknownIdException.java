package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class UnknownIdException extends RuntimeException {
  private Message message;

  public UnknownIdException(Message message) {
    super(message.toString());
    this.message = message;
  }

  public Message asMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message.toString();
  }
}

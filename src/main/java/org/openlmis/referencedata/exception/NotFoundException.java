package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class NotFoundException extends RuntimeException {
  private Message message;

  public NotFoundException(Message message) {
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

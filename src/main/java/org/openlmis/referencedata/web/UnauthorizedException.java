package org.openlmis.referencedata.web;

import org.openlmis.referencedata.util.Message;

public class UnauthorizedException extends RuntimeException {

  private final Message message;

  public UnauthorizedException(Message message) {
    super();
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

package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

public class NotFoundException extends BaseMessageException {

  public NotFoundException(Message message) {
    super(message);
  }
}

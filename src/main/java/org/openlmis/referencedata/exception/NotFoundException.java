package org.openlmis.referencedata.exception;

import org.openlmis.referencedata.util.Message;

/**
 * Exception for indicating that an entity explicitly asked for wasn't found.  This should result
 * in a NOT FOUND api response.
 */
public class NotFoundException extends BaseMessageException {

  public NotFoundException(Message message) {
    super(message);
  }
}

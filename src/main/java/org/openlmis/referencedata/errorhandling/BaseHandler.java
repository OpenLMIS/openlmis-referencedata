package org.openlmis.referencedata.errorhandling;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class BaseHandler {

  @Autowired
  private MessageService messageService;

  /**
   * Translate the Message in a ValidationMessageException into a LocalizedMessage.
   *
   * @param exception is any ValidationMessageException containing a Message
   * @return a LocalizedMessage translated by the MessageService bean
   */
  protected final LocalizedMessage getLocalizedMessage(
      ValidationMessageException exception) {
    return messageService.localize(exception.asMessage());
  }

}

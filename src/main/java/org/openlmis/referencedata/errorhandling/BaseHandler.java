package org.openlmis.referencedata.errorhandling;

import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class BaseHandler {

  @Autowired
  private MessageService messageService;

  /**
   * Translate the Message into a LocalizedMessage.
   *
   * @param message a Message to translate
   * @return a LocalizedMessage translated by the MessageService bean
   */
  protected final LocalizedMessage getLocalizedMessage(Message message) {
    return messageService.localize(message);
  }

}

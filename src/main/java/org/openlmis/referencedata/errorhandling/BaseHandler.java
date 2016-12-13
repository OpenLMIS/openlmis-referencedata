package org.openlmis.referencedata.errorhandling;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class BaseHandler {

  @Autowired
  private MessageService messageService;

  public Message.LocalizedMessage getLocalizedMessage(ValidationMessageException exception) {
    return messageService.localize(exception.asMessage());
  }

}
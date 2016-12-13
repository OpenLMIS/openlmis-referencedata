package org.openlmis.referencedata.i18n;

import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageService {

  @Autowired
  private ExposedMessageSource messageSource;

  public Message.LocalizedMessage localize(Message message) {
    return message.localMessage(messageSource, LocaleContextHolder.getLocale());
  }

}
package org.openlmis.referencedata.i18n;

import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  @Autowired
  private ExposedMessageSource messageSource;

  public LocalizedMessage localize(Message message) {
    return message.localMessage(messageSource, LocaleContextHolder.getLocale());
  }

}

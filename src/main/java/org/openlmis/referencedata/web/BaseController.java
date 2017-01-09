package org.openlmis.referencedata.web;

import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.util.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
public abstract class BaseController {

  @Autowired
  RightService rightService;

  @Autowired
  private ExposedMessageSource messageSource;

  protected Map<String, String> getErrors(Errors errors) {
    return errors
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
  }

  protected ErrorResponse buildErrorResponse(String messageKey) {
    return buildErrorResponse(messageKey, null);
  }

  protected ErrorResponse buildErrorResponse(String messageKey, Object[] errorArgs) {
    return new ErrorResponse(messageKey,
        messageSource.getMessage(messageKey, errorArgs, LocaleContextHolder.getLocale()));
  }
}

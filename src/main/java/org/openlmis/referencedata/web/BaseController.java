package org.openlmis.referencedata.web;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
public abstract class BaseController {

  protected Map<String, String> getErrors(Errors errors) {
    return errors
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
  }

}

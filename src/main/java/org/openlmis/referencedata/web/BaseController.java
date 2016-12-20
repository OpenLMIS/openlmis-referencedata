package org.openlmis.referencedata.web;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.json.JsonConverter;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.util.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
public abstract class BaseController {

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private Javers javers;

  protected Map<String, String> getErrors(Errors errors) {
    return errors
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
  }

  protected ErrorResponse buildErrorResponse(String messageKey) {
    return new ErrorResponse(messageKey,
        messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale()));
  }


  protected String getChangesByClass(Class type)
  {
    /* Depending on the business' preference, we can either use findSnapshots() or findChanges(). */
    //javers.findSnapshots(QueryBuilder.byClass(Facility.class))
    List<Change> changes = javers.findChanges(QueryBuilder.byClass(type).build());

    changes.sort((o1, o2) -> -1 * o1.getCommitMetadata().get().getCommitDate().compareTo(o2.getCommitMetadata().get().getCommitDate()));
    JsonConverter jsonConverter = javers.getJsonConverter();
    return jsonConverter.toJson(changes);
  }


}

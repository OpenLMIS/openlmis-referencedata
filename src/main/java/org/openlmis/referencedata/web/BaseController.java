package org.openlmis.referencedata.web;

import org.javers.core.Javers;
import org.javers.core.changelog.SimpleTextChangeLog;
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

  private static final int DEFAULT_AUDIT_RESULT_LIMIT = 100;


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


  /**
   * Return a list of changes via JSON.
   * @param type The type of class for which we wish to retrieve historical changes.
   */
  protected String getChangesByClass(Class type)
  {
    return getChangesByClass(type, 0, DEFAULT_AUDIT_RESULT_LIMIT, null, null);
  }

  /**
   * Return a list of changes via JSON.
   *
   * @param type The type of class for which we wish to retrieve historical changes
   * @param skip The number of historical changes to skip. Useful for paging.
   * @param limit The maximum number of historical change results to return. Useful for paging.
   * @param author The author of the changes which should be returned. If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned. If null or empty, changes associated with any and all properties are returned.
   */
  protected String getChangesByClass(Class type, int skip, int limit, String author, String changedPropertyName)
  {
    List<Change> changes = getChangesByType(type, skip, limit, author, changedPropertyName);
    JsonConverter jsonConverter = javers.getJsonConverter();
    return jsonConverter.toJson(changes);
  }


  /**
   * Return a list of changes as a log (in other words, as a series of line entries).
   * @param type The type of class for which we wish to retrieve historical changes.
   */
  protected String getChangeLogByClass(Class type)
  {
    return getChangeLogByClass(type, 0, DEFAULT_AUDIT_RESULT_LIMIT, null, null);
  }

  /**
   * Return a list of changes as a log (in other words, as a series of line entries).
   * The available parameters and their means are the same as for the getChangesByClass() method.
   */
  protected String getChangeLogByClass(Class type, int skip, int limit, String author, String changedPropertyName)
  {
    List<Change> changes = getChangesByType(type, skip, limit, author, changedPropertyName);
    return javers.processChangeList(changes, new SimpleTextChangeLog());
  }


  /*
    Return JaVers changes for the specified class type.
  */
  private List<Change> getChangesByType(Class type, int skip, int limit, String author, String changedPropertyName)
  {
    QueryBuilder queryBuilder = QueryBuilder.byClass(type).withNewObjectChanges(true).skip(skip).limit(limit);

    if(author != null && !author.isEmpty())
      queryBuilder = queryBuilder.byAuthor(author);
    if(changedPropertyName != null && !changedPropertyName.isEmpty())
      queryBuilder = queryBuilder.andProperty(changedPropertyName);

    /* Depending on the business' preference, we can either use findSnapshots() or findChanges().
       Whereas the former returns the entire state of the object as it was at each commit, the later
       returns only the property and values which changed. */
    //List<Change> changes = javers.findSnapshots(queryBuilder.build());
    List<Change> changes = javers.findChanges(queryBuilder.build());

    changes.sort((o1, o2) -> -1 * o1.getCommitMetadata().get().getCommitDate().compareTo(o2.getCommitMetadata().get().getCommitDate()));
    return changes;
  }

}

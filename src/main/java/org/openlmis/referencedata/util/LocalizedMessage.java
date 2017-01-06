package org.openlmis.referencedata.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.Validate;

/**
 * Value class of a localized message.  Useful for JSON serialization, logging, etc...
 */
public final class LocalizedMessage {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String messageKey;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String message;

  LocalizedMessage(String messageKey, String message) {
    Validate.notBlank(message);
    this.messageKey = messageKey;
    this.message = message;
  }

  @Override
  public String toString() {
    return messageKey + ": " + message;
  }
}

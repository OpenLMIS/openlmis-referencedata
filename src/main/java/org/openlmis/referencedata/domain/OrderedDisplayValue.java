package org.openlmis.referencedata.domain;

import com.google.common.base.Strings;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.exception.ValidationMessageException;

import java.util.Objects;

import javax.persistence.Embeddable;

import lombok.Getter;

/**
 * An OrderedDisplayValue is used for unique values/categories that a user would select.  This is
 * a simple immutable value meant to be used in fuller entities.
 */
@Embeddable
public class OrderedDisplayValue {
  @JsonProperty
  @Getter
  private String displayName;
  @JsonProperty
  @Getter
  private int displayOrder;

  private OrderedDisplayValue() {}

  /**
   * Create a new ordered display value.
   * @param displayName a name for end-user display.
   * @param displayOrder the order of which to display this.
   * @throws ValidationMessageException if displayName is null or blank.
   */
  public OrderedDisplayValue(String displayName, int displayOrder) {
    displayName = displayName.trim();
    if (Strings.isNullOrEmpty(displayName)) {
      throw new ValidationMessageException(
          "referenceData.error.orderedDisplayValue.displayName.empty");
    }

    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof OrderedDisplayValue)) {
      return false;
    }

    OrderedDisplayValue that = (OrderedDisplayValue) object;
    if (displayOrder != that.displayOrder) {
      return false;
    }
    return displayName.equalsIgnoreCase(that.displayName);

  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName.toUpperCase(), displayOrder);
  }
}

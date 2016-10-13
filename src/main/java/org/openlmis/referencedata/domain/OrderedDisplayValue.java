package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

import lombok.Getter;

import java.util.Objects;
import javax.persistence.Embeddable;

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
   * @throws IllegalArgumentException if displayName is null or blank.
   */
  public OrderedDisplayValue(String displayName,
                             int displayOrder) {
    if (Strings.isNullOrEmpty(displayName)) {
      throw new IllegalArgumentException("Display name may not be null or blank");
    }

    this.displayName = displayName.trim();
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

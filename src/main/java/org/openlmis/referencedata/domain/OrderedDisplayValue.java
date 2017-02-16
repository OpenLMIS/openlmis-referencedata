/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.domain;

import com.google.common.base.Strings;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.OrderedDisplayValueMessageKeys;

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
      throw new ValidationMessageException(OrderedDisplayValueMessageKeys.ERROR_DISPLAY_NAME_EMPTY);
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

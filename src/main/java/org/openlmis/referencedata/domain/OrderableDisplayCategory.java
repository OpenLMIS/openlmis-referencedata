package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;

import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Informative category a Product is in when assigned to a {@link Program}.
 */
@Entity
@Table(name = "orderable_display_categories", schema = "referencedata")
public class OrderableDisplayCategory extends BaseEntity {

  @Embedded
  @Getter
  private Code code;

  @Embedded
  @JsonUnwrapped
  @Getter
  private OrderedDisplayValue orderedDisplayValue;

  private OrderableDisplayCategory() {
  }

  /**
   * Creates a new OrderableDisplayCategory.
   *
   * @param code         this OrderableDisplayCategory's unique implementation code.
   * @param displayValue the display values of this OrderableDisplayCategory.
   * @return a new OrderableDisplayCategory.
   * @throws NullPointerException if either parameter is null.
   */
  protected OrderableDisplayCategory(Code code, OrderedDisplayValue displayValue) {
    Objects.requireNonNull(code);
    Objects.requireNonNull(displayValue);
    this.code = code;
    this.orderedDisplayValue = displayValue;
  }

  /**
   * Update this from another.  Copies display values from the other OrderableDisplayCategory
   * into this one.
   *
   * @param orderableDisplayCategory OrderableDisplayCategory to update from.
   */
  public void updateFrom(OrderableDisplayCategory orderableDisplayCategory) {
    this.orderedDisplayValue = orderableDisplayCategory.orderedDisplayValue;
  }

  /**
   * Creates a new OrderableDisplayCategory.
   *
   * @param orderableDisplayCategoryCode this OrderableDisplayCategory's unique implementation code.
   * @return a new OrderableDisplayCategory using default display value and order
   * @throws NullPointerException if parameter is null.
   */
  public static OrderableDisplayCategory createNew(Code orderableDisplayCategoryCode) {
    return OrderableDisplayCategory.createNew(orderableDisplayCategoryCode,
        new OrderedDisplayValue(orderableDisplayCategoryCode.toString(), 1));
  }

  /**
   * Creates a new OrderableDisplayCategory.
   *
   * @param orderableDisplayCategoryCode this OrderableDisplayCategory's unique implementation code.
   * @param displayValue        the display values of this OrderableDisplayCategory.
   * @return a new OrderableDisplayCategory.
   * @throws NullPointerException if either parameter is null.
   */
  public static OrderableDisplayCategory createNew(Code orderableDisplayCategoryCode,
                                                   OrderedDisplayValue displayValue) {
    return new OrderableDisplayCategory(orderableDisplayCategoryCode, displayValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OrderableDisplayCategory)) {
      return false;
    }
    OrderableDisplayCategory that = (OrderableDisplayCategory) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }
}
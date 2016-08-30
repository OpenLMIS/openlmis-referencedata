package org.openlmis.referencedata.domain;

import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * Represents a unique product's designation.
 */
@Embeddable
@NoArgsConstructor
public final class ProductCode {
  private String productCode;

  private ProductCode(String productCode) {
    this.productCode = productCode.replaceAll("\\s", "");
  }

  /**
   * ProductCode equality ignores whitespace and case.
   * @param object the ProductCode to test against.
   * @return true if both represent the same code, false otherwise.
   */
  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof ProductCode)) {
      return false;
    }

    return ((ProductCode) object).productCode.equalsIgnoreCase(this.productCode);
  }

  @Override
  public final int hashCode() {
    return productCode.toUpperCase().hashCode();
  }

  @Override
  public String toString() {
    return productCode;
  }

  /**
   * Creates a new ProductCode value.
   * @param productCode the product's code
   * @return a new ProductCode with the given code.  Uses a blank code if given null.
   */
  public static final ProductCode newProductCode(String productCode) {
    String workingCode = (null == productCode) ? "" : productCode;
    return new ProductCode(workingCode);
  }
}

package org.openlmis.referencedata.domain;

/**
 * Represent's a unique product's designation.
 */
public final class ProductCode {
  private String productCode;

  private ProductCode(String productCode) {
    this.productCode = productCode.replaceAll("\\s", "");
  }

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
    return productCode.hashCode();
  }

  @Override
  public String toString() {
    return productCode;
  }

  public static final ProductCode newProductCode(String productCode) {
    return new ProductCode(productCode);
  }
}

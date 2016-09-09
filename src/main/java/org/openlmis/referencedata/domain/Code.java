package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Embeddable;

/**
 * Represents a unique designation.
 */
@Embeddable
public class Code {
  private final String code;

  protected Code() {
    this.code = "";
  }

  private Code(String code) {
    this.code = code.replaceAll("\\s", "");
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

    if (!(object instanceof Code)) {
      return false;
    }

    return this.code.equalsIgnoreCase(((Code) object).code);
  }

  @Override
  public final int hashCode() {
    return code.toUpperCase().hashCode();
  }

  @Override
  @JsonValue
  public String toString() {
    return code;
  }

  /**
   * Creates a new ProductCode value.
   * @param productCode the product's code
   * @return a new ProductCode with the given code.  Uses a blank code if given null.
   */
  @JsonCreator
  public static final Code code(String productCode) {
    String workingCode = (null == productCode) ? "" : productCode;
    return new Code(workingCode);
  }
}

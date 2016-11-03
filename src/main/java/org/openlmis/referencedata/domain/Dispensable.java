package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Embeddable;


@Embeddable
public class Dispensable {

  private final String dispensingUnit;

  protected Dispensable() {
    this.dispensingUnit = "";
  }

  protected Dispensable(String dispensingUnit) {
    this.dispensingUnit = dispensingUnit;
  }

  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof Dispensable)) {
      return false;
    }

    return this.dispensingUnit.equalsIgnoreCase(((Dispensable) object).dispensingUnit);
  }

  @Override
  public final int hashCode() {
    return dispensingUnit.hashCode();
  }

  @Override
  @JsonValue
  public String toString() {
    return dispensingUnit;
  }

  @JsonCreator
  public static final Dispensable createNew(String dispensingUnit) {
    return new Dispensable(dispensingUnit);
  }

}

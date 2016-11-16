package org.openlmis.referencedata.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * A Dispensable describes how product is dispensed/given to a patient.
 * Description of the Dispensable contains information about product form,
 * dosage, dispensing unit etc.
 */
@Embeddable
public class Dispensable {

  @Getter
  private final String dispensingUnit;

  protected Dispensable() {
    this.dispensingUnit = "";
  }

  protected Dispensable(String dispensingUnit) {
    this.dispensingUnit = dispensingUnit.trim();
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
    return dispensingUnit.toLowerCase().hashCode();
  }

  @Override
  public String toString() {
    return dispensingUnit;
  }

  public static final Dispensable createNew(String dispensingUnit) {
    String correctDispensingUnit = (null == dispensingUnit) ? "" : dispensingUnit;
    return new Dispensable(correctDispensingUnit);
  }

}

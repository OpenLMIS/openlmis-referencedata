package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Products that are Orderable by Program.  An Orderable represent any medical commodities
 * that may be ordered/requisitioned, typically by a {@link Program}.
 */
@Entity
@DiscriminatorColumn(name = "Type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "orderables", schema = "referencedata")
@NoArgsConstructor
public abstract class Orderable extends BaseEntity {
  @Embedded
  private Code productCode;

  @Embedded
  private Dispensable dispensable;

  private String name;

  @JsonProperty
  @Getter(AccessLevel.PACKAGE)
  private long packSize;

  @JsonProperty
  private long packRoundingThreshold;

  @JsonProperty
  private boolean roundToZero;

  @OneToMany(mappedBy = "orderable", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<ProgramOrderable> programOrderables;

  protected Orderable(Code productCode, Dispensable dispensable, String name, long packSize,
                      long packRoundingThreshold, boolean roundToZero) {
    this.productCode = productCode;
    this.dispensable = dispensable;
    this.name = name;
    this.packSize = packSize;
    this.packRoundingThreshold = packRoundingThreshold;
    this.roundToZero = roundToZero;
    this.programOrderables = new LinkedHashSet<>();
  }

  public boolean hasProgram() {
    return null != programOrderables && 0 < programOrderables.size();
  }

  /**
   * Return this orderable product's unique product code.
   * @return a copy of this product's unique product code.
   */
  @JsonProperty
  public final Code getProductCode() {
    return productCode;
  }

  @JsonProperty
  public final Dispensable getDispensable() {
    return dispensable;
  }

  /**
   * Return this orderable product's name.
   * @return this product's name.
   */
  @JsonProperty
  public final String getName() {
    return name;
  }

  /**
   * Adds product for ordering within a program.
   * @param programOrderable the association to a {@link org.openlmis.referencedata.domain.Program}
   * @return true if successful, false otherwise.
   */
  public final boolean addToProgram(ProgramOrderable programOrderable) {
    if (programOrderables.contains(programOrderable)) {
      programOrderables.remove(programOrderable);
    }

    return programOrderables.add(programOrderable);
  }

  @JsonProperty
  protected final void setPrograms(Set<ProgramOrderableBuilder> ppBuilders) {
    Set<ProgramOrderable> workProgProducts = new HashSet<>();

    // add or modify associations
    for (ProgramOrderableBuilder ppBuilder : ppBuilders) {
      ProgramOrderable programOrderable = ppBuilder.createProgramOrderable(this);
      workProgProducts.add(programOrderable);
      addToProgram(programOrderable);
    }
    this.programOrderables.retainAll(workProgProducts); // remove old associations
  }

  @JsonProperty
  protected final Set<ProgramOrderable> getPrograms() {
    return programOrderables;
  }

  /**
   * Get the association to a {@link Program}.
   * @param program the Program this product is (maybe) in.
   * @return the asssociation to the given {@link Program}, or null if this product is not in the
   *        given program.
   */
  public ProgramOrderable getProgramOrderable(Program program) {
    for (ProgramOrderable programOrderable : programOrderables) {
      if (programOrderable.isForProgram(program)) {
        return programOrderable;
      }
    }

    return null;
  }

  @JsonProperty
  public abstract String getDescription();

  /**
   * Returns the number of packs to order. For this Orderable given a desired number of
   * dispensing units, will return the number of packs that should be ordered.
   * @param dispensingUnits # of dispensing units we'd like to order for
   * @return the number of packs that should be ordered.
   */
  public long packsToOrder(long dispensingUnits) {
    if (dispensingUnits <= 0 || packSize == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / packSize;
    long remainderQuantity = dispensingUnits % packSize;

    if (remainderQuantity > 0 && remainderQuantity > packRoundingThreshold) {
      packsToOrder += 1;
    }

    if (packsToOrder == 0 && !roundToZero) {
      packsToOrder = 1;
    }

    return packsToOrder;
  }

  /**
   * Determines if product may be used to fulfill for the given product.
   * @param product the product we'd like to fulfill for.
   * @return true if this product can fulfill for the given product.  False otherwise.
   */
  public abstract boolean canFulfill(Orderable product);

  /**
   * Determines equality based on product codes.
   * @param object another Orderable, ideally.
   * @return true if the two are semantically equal.  False otherwise.
   */
  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof Orderable)) {
      return false;
    }

    return ((Orderable) object).productCode.equals(this.productCode);
  }

  @Override
  public final int hashCode() {
    return productCode.hashCode();
  }
}

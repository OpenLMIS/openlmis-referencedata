package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Products that are Orderable by Program.  An OrderableProduct represent any medical commodities
 * that may be ordered/requisitioned, typically by a {@link Program}.
 */
@Entity
@DiscriminatorColumn(name = "Type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "orderable_products", schema = "referencedata")
@NoArgsConstructor
@SuppressWarnings({"PMD.UnusedPrivateField"})
public abstract class OrderableProduct extends BaseEntity {
  @Embedded
  private Code productCode;

  @Embedded
  private Dispensable dispensable;

  private String name;

  @JsonProperty
  private long packSize;

  @JsonProperty
  private long packRoundingThreshold;

  @JsonProperty
  private boolean roundToZero;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<ProgramProduct> programProducts;

  protected OrderableProduct(Code productCode, Dispensable dispensable, String name, long packSize,
                             long packRoundingThreshold, boolean roundToZero) {
    this.productCode = productCode;
    this.dispensable = dispensable;
    this.name = name;
    this.packSize = packSize;
    this.packRoundingThreshold = packRoundingThreshold;
    this.roundToZero = roundToZero;
    this.programProducts = new LinkedHashSet<>();
  }

  public boolean hasProgram() {
    return null != programProducts && 0 < programProducts.size();
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
   * @param programProduct the association to a {@link org.openlmis.referencedata.domain.Program}
   * @return true if successful, false otherwise.
   */
  public final boolean addToProgram(ProgramProduct programProduct) {
    if (programProducts.contains(programProduct)) {
      programProducts.remove(programProduct);
    }

    return programProducts.add(programProduct);
  }

  @JsonProperty
  protected final void setPrograms(Set<ProgramProductBuilder> ppBuilders) {
    Set<ProgramProduct> workProgProducts = new HashSet<>();

    // add or modify associations
    for (ProgramProductBuilder ppBuilder : ppBuilders) {
      ProgramProduct programProduct = ppBuilder.createProgramProduct(this);
      workProgProducts.add(programProduct);
      addToProgram(programProduct);
    }
    this.programProducts.retainAll(workProgProducts); // remove old associations
  }

  @JsonProperty
  protected final Set<ProgramProduct> getPrograms() {
    return programProducts;
  }

  /**
   * Get the association to a {@link Program}.
   * @param program the Program this product is (maybe) in.
   * @return the asssociation to the given {@link Program}, or null if this product is not in the
   *        given program.
   */
  public ProgramProduct getProgramProduct(Program program) {
    for (ProgramProduct programProduct : programProducts) {
      if (programProduct.isForProgram(program)) {
        return programProduct;
      }
    }

    return null;
  }

  @JsonProperty
  public abstract String getDescription();

  /**
   * Returns the number of packs to order. For this OrderableProduct given a desired number of
   * dispensing units, will return the number of packs that should be ordered.
   * @param dispensingUnits # of dispensing units we'd like to order for
   * @return the number of packs that should be ordered.
   */
  public long packsToOrder(long dispensingUnits) {
    double fractionalPacks = (double) (dispensingUnits) / packSize;
    return 0 >= fractionalPacks ? 0 : (long) Math.ceil(fractionalPacks);
  }

  /**
   * Determines if product may be used to fulfill for the given product.
   * @param product the product we'd like to fulfill for.
   * @return true if this product can fulfill for the given product.  False otherwise.
   */
  public abstract boolean canFulfill(OrderableProduct product);

  /**
   * Determines equality based on product codes.
   * @param object another OrderableProduct, ideally.
   * @return true if the two are semantically equal.  False otherwise.
   */
  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof OrderableProduct)) {
      return false;
    }

    return ((OrderableProduct) object).productCode.equals(this.productCode);
  }

  @Override
  public final int hashCode() {
    return productCode.hashCode();
  }
}

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

  private String fullProductName;

  @JsonProperty
  @Getter(AccessLevel.PACKAGE)
  private long netContent;

  @JsonProperty
  private long packRoundingThreshold;

  @JsonProperty
  private boolean roundToZero;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<ProgramOrderable> programOrderables;

  protected Orderable(Code productCode, Dispensable dispensable, String fullProductName,
                      long netContent, long packRoundingThreshold, boolean roundToZero) {
    this.productCode = productCode;
    this.dispensable = dispensable;
    this.fullProductName = fullProductName;
    this.netContent = netContent;
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
  public final String getFullProductName() {
    return fullProductName;
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
    if (dispensingUnits <= 0 || netContent == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / netContent;
    long remainderQuantity = dispensingUnits % netContent;

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

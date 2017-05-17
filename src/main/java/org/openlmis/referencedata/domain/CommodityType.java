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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * CommodityTypes are generic commodities to simplify ordering and use.  A CommodityType doesn't
 * have a single manufacturer, nor a specific packaging.  Instead a CommodityType represents a
 * refined categorization of products that may typically be ordered / exchanged for one another.
 */
@Entity
@DiscriminatorValue("COMMODITY_TYPE")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CommodityType extends Orderable {
  private String description;

  @Getter
  private String classificationSystem;

  @Getter
  private String classificationId;

  @Getter
  @ManyToOne
  @JoinColumn(columnDefinition = "parentid")
  private CommodityType parent;

  @Getter
  @Setter
  @OneToMany(mappedBy = "parent")
  @JsonIgnore
  private List<CommodityType> children;

  private CommodityType(Code productCode, Dispensable dispensable, String fullProductName,
                        long netContent, long packRoundingThreshold, boolean roundToZero,
                        String classificationSystem, String classificationId) {
    super(productCode, dispensable, fullProductName, netContent, packRoundingThreshold,
          roundToZero);
    this.classificationSystem = classificationSystem;
    this.classificationId = classificationId;
    this.children = new ArrayList<>();
  }

  /**
   * Create a new commodity type.
   *
   * @param productCode a unique product code
   * @param fullProductName fullProductName of product
   * @param description the description to display in ordering, fulfilling, etc
   * @param netContent    the number of dispensing units in the pack
   * @param packRoundingThreshold determines how number of packs is rounded
   * @param roundToZero determines if number of packs can be rounded to zero
   * @return a new CommodityType
   */
  @JsonCreator
  public static CommodityType newCommodityType(
      @JsonProperty("productCode") String productCode,
      @JsonProperty("dispensingUnit")
         String dispensingUnit,
      @JsonProperty("fullProductName") String fullProductName,
      @JsonProperty("description") String description,
      @JsonProperty("netContent") long netContent,
      @JsonProperty("packRoundingThreshold")
           long packRoundingThreshold,
      @JsonProperty("roundToZero") boolean roundToZero,
      @JsonProperty("classificationSystem") String classificationSystem,
      @JsonProperty("classificationId") String classificationId) {
    Code code = Code.code(productCode);
    Dispensable dispensable = Dispensable.createNew(dispensingUnit);
    CommodityType commodityType = new CommodityType(code, dispensable, fullProductName, netContent,
        packRoundingThreshold, roundToZero, classificationSystem, classificationId);
    commodityType.description = description;
    return commodityType;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean canFulfill(Orderable product) {
    return this.equals(product);
  }

  /**
   * Validates and assigns a parent to this commodity type.
   * No cycles in the hierarchy are allowed.
   *
   * @param parent the parent to assign
   */
  public void assignParent(CommodityType parent) {
    validateIsNotDescendant(parent);

    this.parent = parent;
    parent.children.add(this);
  }

  private void validateIsNotDescendant(CommodityType commodityType) {
    for (CommodityType child : children) {
      if (child.equals(commodityType)) {
        throw new ValidationMessageException(new Message(
            CommodityTypeMessageKeys.ERROR_PARENT_IS_DESCENDANT,
            commodityType.getId(), id));
      }
      child.validateIsNotDescendant(commodityType);
    }
  }
}

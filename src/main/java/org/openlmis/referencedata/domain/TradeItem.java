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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.ProductMessageKeys;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * TradeItems represent branded/produced/physical products.  A TradeItem is used for Product's that
 * are made and then bought/sold/exchanged.  Unlike a {@link CommodityType} a TradeItem usually
 * has one and only one manufacturer and is shipped in exactly one primary package.
 *
 * <p>TradeItem's also may:
 * <ul>
 *   <li>have a GlobalTradeItemNumber</li>
 *   <li>a MSRP</li>
 * </ul>
 */
@Entity
@DiscriminatorValue("TRADE_ITEM")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TradeItem extends Orderable {

  @JsonProperty
  private String manufacturerOfTradeItem;

  @OneToMany(mappedBy = "tradeItem", cascade = CascadeType.ALL)
  @JsonProperty
  @Getter
  @Setter
  private List<TradeItemClassification> classifications;

  private TradeItem(Code productCode, Dispensable dispensable, String fullProductName,
                    long netContent, long packRoundingThreshold, boolean roundToZero) {
    super(productCode, dispensable, fullProductName, netContent, packRoundingThreshold,
          roundToZero);
    classifications = new ArrayList<>();
  }

  @Override
  public String getDescription() {
    return manufacturerOfTradeItem;
  }

  /**
   * A TradeItem can fulfill for the given product if the product is this trade item or if this
   * product's CommodityType is the given product.
   * @param product the product we'd like to fulfill for.
   * @return true if we can fulfill for the given product, false otherwise.
   */
  @Override
  public boolean canFulfill(Orderable product) {
    // TODO: OLMIS-1696
    return this.equals(product);
  }

  /**
   * Factory method to create a new trade item.
   * @param productCode a unique product code
   * @param fullProductName fullProductName of product
   * @param netContent the # of dispensing units contained
   * @param packRoundingThreshold determines how number of packs is rounded
   * @param roundToZero determines if number of packs can be rounded to zero
   * @return a new trade item or armageddon if failure
   */
  @JsonCreator
  public static TradeItem newTradeItem(@JsonProperty("productCode") String productCode,
                                       @JsonProperty("dispensingUnit") String dispensingUnit,
                                       @JsonProperty("fullProductName") String fullProductName,
                                       @JsonProperty("netContent") long netContent,
                                       @JsonProperty("packRoundingThreshold")
                                             long packRoundingThreshold,
                                       @JsonProperty("roundToZero") boolean roundToZero) {
    Code code = Code.code(productCode);
    Dispensable dispensable = Dispensable.createNew(dispensingUnit);
    return new TradeItem(code, dispensable, fullProductName, netContent,
        packRoundingThreshold, roundToZero);
  }

  /**
   * Assigns a commodity type to this trade item - will associate this trade item
   * with the classification system of the provided commodity type.
   * @param commodityType the commodity type to associate with
   */
  public void assignCommodityType(CommodityType commodityType) {
    if (hasSameDispensingUnit(commodityType)) {
      assignCommodityType(commodityType.getClassificationSystem(),
          commodityType.getClassificationId());
    } else {
      throw new ValidationMessageException(ProductMessageKeys.ERROR_DISPENSING_UNITS_WRONG);
    }
  }

  /**
   * Assigns to the classification system and classification id.
   * @param classificationSystem the classification system
   * @param classificationId the id of the classification system.
   */
  public void assignCommodityType(String classificationSystem, String classificationId) {
    TradeItemClassification existingClassification = findClassificationById(classificationId);

    if (existingClassification == null) {
      classifications.add(new TradeItemClassification(this, classificationSystem,
          classificationId));
    } else {
      existingClassification.setClassificationSystem(classificationSystem);
    }
  }

  boolean hasSameDispensingUnit(Orderable product) {
    return this.getDispensable().equals(product.getDispensable());
  }

  TradeItemClassification findClassificationById(String classificationId) {
    for (TradeItemClassification classification : classifications) {
      if (classificationId.equals(classification.getClassificationId())) {
        return classification;
      }
    }
    return null;
  }
}

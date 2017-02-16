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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * CommodityTypes are generic commodities to simplify ordering and use.  A CommodityType doesn't
 * have a single manufacturer, nor a specific packaging.  Instead a CommodityType represents a
 * refined categorization of products that may typically be ordered / exchanged for one another.
 */
@Entity
@DiscriminatorValue("COMMODITY_TYPE")
@NoArgsConstructor
public final class CommodityType extends Orderable {
  private String description;

  @OneToMany(mappedBy = "commodityType")
  private Set<TradeItem> tradeItems;

  private CommodityType(Code productCode, Dispensable dispensable, String name, long packSize,
                        long packRoundingThreshold, boolean roundToZero) {
    super(productCode, dispensable, name, packSize, packRoundingThreshold, roundToZero);
    tradeItems = new HashSet<>();
  }

  /**
   * Create a new commodity type.
   *
   * @param productCode a unique product code
   * @param name name of product
   * @param description the description to display in ordering, fulfilling, etc
   * @param packSize    the number of dispensing units in the pack
   * @param packRoundingThreshold determines how number of packs is rounded
   * @param roundToZero determines if number of packs can be rounded to zero
   * @return a new CommodityType
   */
  @JsonCreator
  public static CommodityType newCommodityType(@JsonProperty("productCode") String productCode,
                                               @JsonProperty("dispensingUnit")
                                                   String dispensingUnit,
                                               @JsonProperty("name") String name,
                                               @JsonProperty("description") String description,
                                               @JsonProperty("packSize") long packSize,
                                               @JsonProperty("packRoundingThreshold")
                                                     long packRoundingThreshold,
                                               @JsonProperty("roundToZero") boolean roundToZero) {
    Code code = Code.code(productCode);
    Dispensable dispensable = Dispensable.createNew(dispensingUnit);
    CommodityType commodityType = new CommodityType(code, dispensable, name, packSize,
        packRoundingThreshold, roundToZero);
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
   * Add a TradeItem that can be fulfilled for this CommodityType.
   *
   * @param tradeItem the trade item
   * @return true if added, false if it's already added or was otherwise unable to add.
   */
  public boolean addTradeItem(TradeItem tradeItem) {
    boolean added = tradeItems.add(tradeItem);
    if (added) {
      tradeItem.assignCommodityType(this);
    }

    return added;
  }

  /**
   * Sets the associated {@link TradeItem} that may fulfill for this.
   * @param tradeItems the trade items.
   */
  public void setTradeItems(Set<TradeItem> tradeItems) {
    this.tradeItems.forEach(tradeItem -> tradeItem.assignCommodityType(null));
    this.tradeItems.clear();
    tradeItems.forEach(tradeItem -> addTradeItem(tradeItem));
  }
}

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

import static javax.persistence.FetchType.EAGER;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openlmis.referencedata.dto.OrderableDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
@Table(name = "trade_items", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
public final class TradeItem extends BaseEntity {

  @OneToMany(mappedBy = "tradeItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = EAGER)
  private Set<Orderable> orderables;

  private String manufacturerOfTradeItem;

  @OneToMany(mappedBy = "tradeItem", cascade = CascadeType.ALL)
  @Getter
  private List<TradeItemClassification> classifications;

  /**
   * A TradeItem can fulfill for the given product if the product is this trade item or if this
   * product's CommodityType is the given product.
   * @param product the product we'd like to fulfill for.
   * @return true if we can fulfill for the given product, false otherwise.
   */
  public boolean canFulfill(Orderable product) {
    for (Orderable orderable : orderables) {
      if (orderable.getProductCode().equals(product.getProductCode())) {
        return true;
      }
    }
    for (TradeItemClassification classification : classifications) {
      if (product.getIdentifiers().containsValue(classification.getClassificationId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Assigns a commodity type to this trade item - will associate this trade item
   * with the classification system of the provided commodity type.
   * @param commodityType the commodity type to associate with
   */
  public void assignCommodityType(CommodityType commodityType) {
    assignCommodityType(commodityType.getClassificationSystem(),
            commodityType.getClassificationId());
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

  TradeItemClassification findClassificationById(String classificationId) {
    for (TradeItemClassification classification : classifications) {
      if (classificationId.equals(classification.getClassificationId())) {
        return classification;
      }
    }
    return null;
  }

  /** Creates new instance based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of TradeItem.
   */
  public static TradeItem newInstance(Importer importer) {
    TradeItem tradeItem = new TradeItem();
    tradeItem.id = importer.getId();
    tradeItem.manufacturerOfTradeItem = importer.getManufacturerOfTradeItem();
    if (importer.getClassifications() != null) {
      tradeItem.classifications = importer.getClassifications();
    } else {
      tradeItem.classifications = new ArrayList<>();
    }
    tradeItem.orderables = new HashSet<>();
    if (importer.getOrderables() != null) {
      importer.getOrderables()
          .forEach(oe -> tradeItem.orderables.add(Orderable.newInstance(oe)));
    }

    return tradeItem;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setManufacturerOfTradeItem(manufacturerOfTradeItem);
    exporter.setClassifications(classifications);
    exporter.setOrderables(OrderableDto.newInstance(orderables));
  }

  public interface Importer {
    UUID getId();

    Set<OrderableDto> getOrderables();

    String getManufacturerOfTradeItem();

    List<TradeItemClassification> getClassifications();
  }

  public interface Exporter {
    void setId(UUID id);

    void setOrderables(Set<OrderableDto> orderables);

    void setManufacturerOfTradeItem(String manufacturerOfTradeItem);

    void setClassifications(List<TradeItemClassification> classifications);
  }
}

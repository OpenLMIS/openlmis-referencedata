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
 * GlobalProducts are generic commodities to simplify ordering and use.  A GlobalProduct doesn't
 * have a single manufacturer, nor a specific packaging.  Instead a GlobalProduct represents a
 * refined categorization of products that may typically be ordered / exchanged for one another.
 */
@Entity
@DiscriminatorValue("GLOBAL_PRODUCT")
@NoArgsConstructor
public final class GlobalProduct extends OrderableProduct {
  private String description;

  @OneToMany(mappedBy = "globalProduct")
  private Set<TradeItem> tradeItems;

  private GlobalProduct(Code productCode, long packSize) {
    super(productCode, packSize);
    tradeItems = new HashSet<>();
  }

  /**
   * Create a new global product.
   *
   * @param productCode a unique product code
   * @param description the description to display in ordering, fulfilling, etc
   * @param packSize    the number of dispensing units in the pack
   * @return a new GlobalProduct
   */
  @JsonCreator
  public static GlobalProduct newGlobalProduct(@JsonProperty("productCode") String productCode,
                                               @JsonProperty("description") String description,
                                               @JsonProperty("packSize") long packSize) {
    Code code = Code.code(productCode);
    GlobalProduct globalProduct = new GlobalProduct(code, packSize);
    globalProduct.description = description;
    return globalProduct;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean canFulfill(OrderableProduct product) {
    return (this.equals(product)) ? true : false;
  }

  /**
   * Add a TradeItem that can be fulfilled for this GlobalProduct.
   *
   * @param tradeItem the trade item
   * @return true if added, false if it's already added or was otherwise unable to add.
   */
  public boolean addTradeItem(TradeItem tradeItem) {
    boolean added = tradeItems.add(tradeItem);
    if (added) {
      tradeItem.assignGlobalProduct(this);
    }

    return added;
  }
}

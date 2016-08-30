package org.openlmis.referencedata.domain;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * TradeItems represent branded/produced/physical products.  A TradeItem is used for Product's that
 * are made and then bought/sold/exchanged.  Unlike a {@link GlobalProduct} a TradeItem usually
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
public final class TradeItem extends OrderableProduct {
  private String manufacturer;

  @ManyToOne
  private GlobalProduct globalProduct;

  private TradeItem(ProductCode productCode, long packSize) {
    super(productCode, packSize);
  }

  @Override
  public String getDescription() {
    return manufacturer;
  }

  @Override
  /**
   * A TradeItem can fulfill for the given product if the product is this trade item or if this
   * product's GlobalProduct is the given product.
   * @param product the product we'd like to fulfill for.
   * @returns true if we can fulfill for the given product, false otherwise.
   */
  public boolean canFulfill(OrderableProduct product) {
    return this.equals(product) || globalProduct.equals(product);
  }

  /**
   * Factory method to create a new trade item.
   * @param productCode a unique product code
   * @param packSize the # of dispensing units contained
   * @param globalProduct the global product this can fulfill for
   * @return a new trade item or armageddon if failure
   */
  public static TradeItem newTradeItem(String productCode,
                                       long packSize,
                                       GlobalProduct globalProduct) {
    ProductCode code = ProductCode.newProductCode(productCode);
    TradeItem tradeItem = new TradeItem(code, packSize);
    globalProduct.addTradeItem(tradeItem);

    return tradeItem;
  }

  void assignGlobalProduct(GlobalProduct globalProduct) {
    this.globalProduct = globalProduct;
  }
}

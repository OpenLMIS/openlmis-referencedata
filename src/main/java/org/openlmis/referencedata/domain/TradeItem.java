package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.ProductMessageKeys;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.NoArgsConstructor;

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
public final class TradeItem extends Orderable {
  @JsonProperty
  private String manufacturer;

  @ManyToOne
  private CommodityType commodityType;

  private TradeItem(Code productCode, Dispensable dispensable, String name, long packSize,
                    long packRoundingThreshold, boolean roundToZero) {
    super(productCode, dispensable, name, packSize, packRoundingThreshold, roundToZero);
  }

  @Override
  public String getDescription() {
    return manufacturer;
  }

  /**
   * A TradeItem can fulfill for the given product if the product is this trade item or if this
   * product's CommodityType is the given product.
   * @param product the product we'd like to fulfill for.
   * @return true if we can fulfill for the given product, false otherwise.
   */
  @Override
  public boolean canFulfill(Orderable product) {
    return this.equals(product) || hasCommodityType(product);
  }

  /**
   * Factory method to create a new trade item.
   * @param productCode a unique product code
   * @param name name of product
   * @param packSize the # of dispensing units contained
   * @param packRoundingThreshold determines how number of packs is rounded
   * @param roundToZero determines if number of packs can be rounded to zero
   * @return a new trade item or armageddon if failure
   */
  @JsonCreator
  public static TradeItem newTradeItem(@JsonProperty("productCode") String productCode,
                                       @JsonProperty("dispensingUnit") String dispensingUnit,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("packSize") long packSize,
                                       @JsonProperty("packRoundingThreshold")
                                             long packRoundingThreshold,
                                       @JsonProperty("roundToZero") boolean roundToZero) {
    Code code = Code.code(productCode);
    Dispensable dispensable = Dispensable.createNew(dispensingUnit);
    return new TradeItem(code, dispensable, name, packSize,
        packRoundingThreshold, roundToZero);
  }

  /**
   * Assign a commodity type.
   * @param commodityType the given commodity type, or null to un-assign.
   */
  void assignCommodityType(CommodityType commodityType) {
    if (null == commodityType || hasSameDispensingUnit(commodityType)) {
      this.commodityType = commodityType;
    } else {
      throw new ValidationMessageException(ProductMessageKeys.ERROR_DISPENSING_UNITS_WRONG);
    }
  }

  /*
  returns true if we have a commodity type and the one given has the same product code,
   false otherwise.
   */
  private boolean hasCommodityType(Orderable product) {
    return null != commodityType && commodityType.equals(product);
  }

  private boolean hasSameDispensingUnit(Orderable product) {
    return this.getDispensable().equals(product.getDispensable());
  }
}

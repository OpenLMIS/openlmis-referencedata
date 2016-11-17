package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.Money;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.ProgramProduct;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto extends BaseDto
    implements ProgramProduct.Exporter, ProgramProduct.Importer {

  private OrderableProduct product;

  private Integer dosesPerMonth;

  private boolean active;

  private ProductCategory productCategory;

  private boolean fullSupply;

  private int displayOrder;

  private int maxMonthsStock;

  private Money pricePerPack;
}

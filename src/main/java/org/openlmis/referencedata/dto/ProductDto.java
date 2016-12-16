package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.ProgramProduct;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductDto extends BaseDto
    implements ProgramProduct.Exporter, ProgramProduct.Importer {

  private UUID productId;

  private String productName;

  private UUID productCategoryId;

  private String productCategoryDisplayName;

  private int productCategoryDisplayOrder;

  private boolean active;

  private boolean fullSupply;

  private int displayOrder;

  private int maxMonthsStock;

  private Integer dosesPerMonth;

  private BigDecimal pricePerPack;
}

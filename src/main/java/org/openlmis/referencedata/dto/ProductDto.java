package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.money.Money;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.serializer.MoneyDeserializer;
import org.openlmis.referencedata.serializer.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductDto extends BaseDto
    implements ProgramProduct.Exporter, ProgramProduct.Importer {

  private UUID productId;

  private String productName;

  private Code productCode;

  private Long productPackSize;

  private UUID productCategoryId;

  private String productCategoryDisplayName;

  private int productCategoryDisplayOrder;

  private boolean active;

  private boolean fullSupply;

  private int displayOrder;

  private int maxMonthsOfStock;

  private Integer dosesPerMonth;

  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money pricePerPack;

}

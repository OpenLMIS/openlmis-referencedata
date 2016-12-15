package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.openlmis.referencedata.domain.Money;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.ProgramProduct;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
@JsonSerialize(using = ProductDto.ProductDtoSerializer.class)
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

  /**
   * JSON Serializer for ProgramProducts.
   */
  public static class ProductDtoSerializer extends StdSerializer<ProductDto> {

    public ProductDtoSerializer() {
      this(null);
    }

    public ProductDtoSerializer(Class<ProductDto> productDtoClass) {
      super(productDtoClass);
    }

    @Override
    public void serialize(ProductDto productDto, JsonGenerator generator,
                          SerializerProvider provider) throws IOException {
      generator.writeStartObject();
      generator.writeStringField("productId", productDto.product.getId().toString());
      generator.writeStringField("productName", productDto.product.getName());
      generator.writeStringField("productCategoryId", productDto.productCategory.getId()
          .toString());
      generator.writeStringField("productCategoryDisplayName",
          productDto.productCategory.getOrderedDisplayValue().getDisplayName());
      generator.writeNumberField("productCategoryDisplayOrder",
          productDto.productCategory.getOrderedDisplayValue().getDisplayOrder());
      generator.writeBooleanField("active", productDto.active);
      generator.writeBooleanField("fullSupply", productDto.fullSupply);
      generator.writeNumberField("displayOrder", productDto.displayOrder);
      generator.writeNumberField("maxMonthsOfStock", productDto.maxMonthsStock);
      if (null != productDto.dosesPerMonth) {
        generator.writeNumberField("dosesPerMonth", productDto.dosesPerMonth);
      }
      if (null != productDto.pricePerPack) {
        generator.writeNumberField("pricePerPack", productDto.pricePerPack.getValue());
      }
      generator.writeEndObject();
    }
  }
}

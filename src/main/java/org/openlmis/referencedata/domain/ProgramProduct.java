package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "program_products", schema = "referencedata")
@NoArgsConstructor
@JsonSerialize(using = ProgramProduct.ProgramProductSerializer.class)
public class ProgramProduct extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  private Program program;

  @ManyToOne
  @JoinColumn(name = "productId", nullable = false)
  @Getter
  @Setter
  @JsonIgnore
  private OrderableProduct product;

  private Integer dosesPerMonth;
  private boolean active;

  //@ManyToOne
  //@JoinColumn(name = "productCategoryId", nullable = false)
  //@Getter
  //@Setter
  private String productCategory;

  private boolean fullSupply;
  private int displayOrder;
  private int maxMonthsStock;

  private ProgramProduct(Program program,
                        OrderableProduct product,
                        String productCategory) {
    this.program = program;
    this.product = product;
    this.productCategory = productCategory;
    this.dosesPerMonth = null;
    this.active = true;
    this.fullSupply = true;
    this.displayOrder = 0;
    this.maxMonthsStock = 0;
  }

  /**
   * Create program product.
   * @param program The Program this Product will be in.
   * @param category the category this Product will be in, in this Program.
   * @param product the Product.
   * @param dosesPerMonth the number of doses a patient needs of this product in a month.
   * @param active weather this product is active in this program at this time.
   * @param displayOrder the display order of this Product in this category of this Program.
   * @param maxMonthsStock the maximum months of stock this product should be stocked for in this
   *                       Program.
   * @return a new ProgramProduct.
   */
  public static final ProgramProduct createNew(Program program,
                                               String category,
                                               OrderableProduct product,
                                               int dosesPerMonth,
                                               boolean active,
                                               boolean fullSupply,
                                               int displayOrder,
                                               int maxMonthsStock) {
    ProgramProduct programProduct = new ProgramProduct(program,
        product,
        category);
    programProduct.dosesPerMonth = dosesPerMonth;
    programProduct.active = active;
    programProduct.fullSupply = fullSupply;
    programProduct.displayOrder = displayOrder;
    programProduct.maxMonthsStock = maxMonthsStock;

    return programProduct;
  }


  public static class ProgramProductSerializer extends StdSerializer<ProgramProduct> {
    public ProgramProductSerializer() {
      this(null);
    }

    public ProgramProductSerializer(Class<ProgramProduct> programProductClass) {
      super(programProductClass);
    }

    @Override
    public void serialize(ProgramProduct programProduct, JsonGenerator generator,
                          SerializerProvider provider) throws IOException, JsonProcessingException {
      generator.writeStartObject();
      generator.writeStringField("programId", programProduct.program.getId().toString());
      generator.writeStringField("productCategory", programProduct.productCategory);
      generator.writeNumberField("dosesPerMonth", programProduct.dosesPerMonth);
      generator.writeBooleanField("active", programProduct.active);
      generator.writeBooleanField("fullSupply", programProduct.fullSupply);
      generator.writeNumberField("displayOrder", programProduct.displayOrder);
      generator.writeNumberField("maxMonthsOfStock", programProduct.maxMonthsStock);
      generator.writeEndObject();
    }
  }
}

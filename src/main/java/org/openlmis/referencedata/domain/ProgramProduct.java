package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;
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
  @Getter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "productId", nullable = false)
  @Getter
  private OrderableProduct product;

  private Integer dosesPerMonth;
  private boolean active;

  //@ManyToOne
  //@JoinColumn(name = "productCategoryId", nullable = false)
  @Getter
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
   * Returns true if this association is for given Program.
   * @param program the {@link Program} to ask about
   * @return true if this association is for the given Program, false otherwise.
   */
  public boolean isForProgram(Program program) {
    return this.program.equals(program);
  }

  /**
   * Create program product association.
   * See {@link #createNew(Program, String, OrderableProduct, Integer, boolean, boolean, int, int)}.
   * Uses sensible defaults.
   * @param program see other
   * @param category see other
   * @param product see other
   * @return see other
   */
  public static final ProgramProduct createNew(Program program,
                                               String category,
                                               OrderableProduct product) {
    ProgramProduct programProduct = new ProgramProduct(program, product, category);
    return programProduct;
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
                                               Integer dosesPerMonth,
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

  /**
   * Equal if both represent association between same Program and Product.  e.g. Ibuprofen in the
   * Essential Meds Program is always the same association regardless of the other properties.
   * @param other the other ProgramProduct
   * @return true if for same Program-Product association, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (Objects.isNull(other) || !(other instanceof ProgramProduct)) {
      return false;
    }

    ProgramProduct otherProgProduct = (ProgramProduct) other;
    return program.equals(otherProgProduct.program) && product.equals(otherProgProduct.product);
  }

  @Override
  public int hashCode() {
    return Objects.hash(program, product);
  }

  /**
   * JSON Serializer for ProgramProducts.
   */
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
      generator.writeBooleanField("active", programProduct.active);
      generator.writeBooleanField("fullSupply", programProduct.fullSupply);
      generator.writeNumberField("displayOrder", programProduct.displayOrder);
      generator.writeNumberField("maxMonthsOfStock", programProduct.maxMonthsStock);
      if (null != programProduct.dosesPerMonth) {
        generator.writeNumberField("dosesPerMonth", programProduct.dosesPerMonth);
      }
      generator.writeEndObject();
    }
  }
}

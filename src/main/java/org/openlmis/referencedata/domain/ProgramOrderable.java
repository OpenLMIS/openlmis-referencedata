package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.CurrencyConfig;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "program_orderables", schema = "referencedata")
@NoArgsConstructor
@JsonSerialize(using = ProgramOrderable.ProgramOrderableSerializer.class)
public class ProgramOrderable extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "productId", nullable = false)
  @Getter
  private Orderable product;

  private Integer dosesPerMonth;

  @Getter
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "orderableDisplayCategoryId", nullable = false)
  @Getter
  private OrderableDisplayCategory orderableDisplayCategory;

  @Getter
  private boolean fullSupply;
  private int displayOrder;
  private int maxMonthsStock;

  @Getter
  @Setter
  @Type(type = "org.jadira.usertype.moneyandcurrency.joda.PersistentMoneyAmount",
      parameters = {@Parameter(name = "currencyCode", value = CurrencyConfig.CURRENCY_CODE)})
  private Money pricePerPack;

  private ProgramOrderable(Program program,
                           Orderable product,
                           OrderableDisplayCategory orderableDisplayCategory) {
    this.program = program;
    this.product = product;
    this.orderableDisplayCategory = orderableDisplayCategory;
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
   * See {@link #createNew(Program,
   *  OrderableDisplayCategory,
   *  Orderable,
   *  Integer,
   *  boolean,
   *  boolean,
   *  int,
   *  int,
   *  Money,
   *  CurrencyUnit)}.
   * Uses sensible defaults.
   * @param program see other
   * @param category see other
   * @param product see other
   * @return see other
   */
  public static final ProgramOrderable createNew(Program program,
                                                 OrderableDisplayCategory category,
                                                 Orderable product,
                                                 CurrencyUnit currencyUnit) {
    ProgramOrderable programOrderable = new ProgramOrderable(program, product, category);
    programOrderable.pricePerPack = Money.of(currencyUnit, BigDecimal.ZERO);
    return programOrderable;
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
   * @param pricePerPack the price of one pack.
   * @return a new ProgramOrderable.
   */
  public static final ProgramOrderable createNew(Program program,
                                                 OrderableDisplayCategory category,
                                                 Orderable product,
                                                 Integer dosesPerMonth,
                                                 boolean active,
                                                 boolean fullSupply,
                                                 int displayOrder,
                                                 int maxMonthsStock,
                                                 Money pricePerPack,
                                                 CurrencyUnit currencyUnit) {
    ProgramOrderable programOrderable = createNew(program, category, product, currencyUnit);
    programOrderable.dosesPerMonth = dosesPerMonth;
    programOrderable.active = active;
    programOrderable.fullSupply = fullSupply;
    programOrderable.displayOrder = displayOrder;
    programOrderable.maxMonthsStock = maxMonthsStock;
    if (pricePerPack != null) {
      programOrderable.pricePerPack = pricePerPack;
    }
    return programOrderable;
  }

  /**
   * Equal if both represent association between same Program and Product.  e.g. Ibuprofen in the
   * Essential Meds Program is always the same association regardless of the other properties.
   * @param other the other ProgramOrderable
   * @return true if for same Program-Orderable association, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (Objects.isNull(other) || !(other instanceof ProgramOrderable)) {
      return false;
    }

    ProgramOrderable otherProgProduct = (ProgramOrderable) other;
    return program.equals(otherProgProduct.program) && product.equals(otherProgProduct.product);
  }

  @Override
  public int hashCode() {
    return Objects.hash(program, product);
  }

  /**
   * JSON Serializer for ProgramOrderables.
   */
  public static class ProgramOrderableSerializer extends StdSerializer<ProgramOrderable> {
    public ProgramOrderableSerializer() {
      this(null);
    }

    public ProgramOrderableSerializer(Class<ProgramOrderable> programOrderableClass) {
      super(programOrderableClass);
    }

    @Override
    public void serialize(ProgramOrderable programOrderable, JsonGenerator generator,
                          SerializerProvider provider) throws IOException {
      generator.writeStartObject();
      generator.writeStringField("programId", programOrderable.program.getId().toString());
      generator.writeStringField("productId", programOrderable.product.getId().toString());
      generator.writeStringField("orderableDisplayCategoryId",
          programOrderable.orderableDisplayCategory.getId().toString());
      generator.writeStringField("productCategoryDisplayName",
          programOrderable.orderableDisplayCategory.getOrderedDisplayValue().getDisplayName());
      generator.writeNumberField("productCategoryDisplayOrder",
          programOrderable.orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder());
      generator.writeBooleanField("active", programOrderable.active);
      generator.writeBooleanField("fullSupply", programOrderable.fullSupply);
      generator.writeNumberField("displayOrder", programOrderable.displayOrder);
      generator.writeNumberField("maxMonthsOfStock", programOrderable.maxMonthsStock);
      if (null != programOrderable.dosesPerMonth) {
        generator.writeNumberField("dosesPerMonth", programOrderable.dosesPerMonth);
      }
      if (null != programOrderable.pricePerPack) {
        generator.writeNumberField("pricePerPack", programOrderable.pricePerPack.getAmount());
      }
      generator.writeEndObject();
    }
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setProductId(product.getId());
    exporter.setProductName(product.getName());
    exporter.setProductCode(product.getProductCode());
    exporter.setProductPackSize(product.getPackSize());
    exporter.setOrderableDisplayCategoryId(orderableDisplayCategory.getId());
    exporter.setProductCategoryDisplayName(
        orderableDisplayCategory.getOrderedDisplayValue().getDisplayName());
    exporter.setProductCategoryDisplayOrder(
        orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder());
    exporter.setActive(active);
    exporter.setFullSupply(fullSupply);
    exporter.setDisplayOrder(displayOrder);
    exporter.setMaxMonthsOfStock(maxMonthsStock);
    exporter.setDosesPerMonth(dosesPerMonth);
    if (pricePerPack != null) {
      exporter.setPricePerPack(pricePerPack);
    }

  }

  public interface Exporter {
    void setId(UUID id);

    void setProductId(UUID productId);

    void setProductName(String productName);

    void setProductCode(Code productCode);

    void setProductPackSize(Long packSize);

    void setOrderableDisplayCategoryId(UUID orderableDisplayCategoryId);

    void setProductCategoryDisplayName(String productCategoryDisplayName);

    void setProductCategoryDisplayOrder(int productCategoryDisplayOrder);

    void setActive(boolean active);

    void setFullSupply(boolean fullSupply);

    void setDisplayOrder(int displayOrder);

    void setMaxMonthsOfStock(int maxMonthsStock);

    void setDosesPerMonth(Integer dosesPerMonth);

    void setPricePerPack(Money pricePerPack);
  }

  public interface Importer {
    UUID getId();

    String getProductName();

    Code getProductCode();

    Long getProductPackSize();

    UUID getOrderableDisplayCategoryId();

    String getProductCategoryDisplayName();

    int getProductCategoryDisplayOrder();

    boolean isActive();

    boolean isFullSupply();

    int getDisplayOrder();

    int getMaxMonthsOfStock();

    Integer getDosesPerMonth();

    Money getPricePerPack();
  }
}

/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
@JsonDeserialize(using = ProgramOrderableDeserializer.class)
public class ProgramOrderable extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "orderableId", nullable = false)
  @Getter
  private Orderable product;

  private Integer dosesPerPatient;

  @Getter
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "orderableDisplayCategoryId", nullable = false)
  @Getter
  private OrderableDisplayCategory orderableDisplayCategory;

  @Getter
  private boolean fullSupply;
  private int displayOrder;

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
    this.dosesPerPatient = null;
    this.active = true;
    this.fullSupply = true;
    this.displayOrder = 0;
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
   * Create program orderable association.
   * See {@link #createNew(Program,
   *  OrderableDisplayCategory,
   *  Orderable,
   *  Integer,
   *  boolean,
   *  boolean,
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
   * Create program orderable.
   * @param program The Program this Product will be in.
   * @param category the category this Product will be in, in this Program.
   * @param product the Product.
   * @param dosesPerPatient the number of doses a patient needs of this orderable.
   * @param active weather this orderable is active in this program at this time.
   * @param displayOrder the display order of this Product in this category of this Program.
   * @param pricePerPack the price of one pack.
   * @return a new ProgramOrderable.
   */
  public static final ProgramOrderable createNew(Program program,
                                                 OrderableDisplayCategory category,
                                                 Orderable product,
                                                 Integer dosesPerPatient,
                                                 boolean active,
                                                 boolean fullSupply,
                                                 int displayOrder,
                                                 Money pricePerPack,
                                                 CurrencyUnit currencyUnit) {
    ProgramOrderable programOrderable = createNew(program, category, product, currencyUnit);
    programOrderable.dosesPerPatient = dosesPerPatient;
    programOrderable.active = active;
    programOrderable.fullSupply = fullSupply;
    programOrderable.displayOrder = displayOrder;
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
      generator.writeStringField("orderableId", programOrderable.product.getId().toString());
      generator.writeStringField("orderableDisplayCategoryId",
          programOrderable.orderableDisplayCategory.getId().toString());
      generator.writeStringField("orderableCategoryDisplayName",
          programOrderable.orderableDisplayCategory.getOrderedDisplayValue().getDisplayName());
      generator.writeNumberField("orderableCategoryDisplayOrder",
          programOrderable.orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder());
      generator.writeBooleanField("active", programOrderable.active);
      generator.writeBooleanField("fullSupply", programOrderable.fullSupply);
      generator.writeNumberField("displayOrder", programOrderable.displayOrder);
      if (null != programOrderable.dosesPerPatient) {
        generator.writeNumberField("dosesPerPatient", programOrderable.dosesPerPatient);
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
    exporter.setOrderableId(product.getId());
    exporter.setOrderableName(product.getName());
    exporter.setOrderableCode(product.getProductCode());
    exporter.setOrderablePackSize(product.getPackSize());
    exporter.setOrderableDisplayCategoryId(orderableDisplayCategory.getId());
    exporter.setOrderableCategoryDisplayName(
        orderableDisplayCategory.getOrderedDisplayValue().getDisplayName());
    exporter.setOrderableCategoryDisplayOrder(
        orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder());
    exporter.setActive(active);
    exporter.setFullSupply(fullSupply);
    exporter.setDisplayOrder(displayOrder);
    exporter.setDosesPerPatient(dosesPerPatient);
    if (pricePerPack != null) {
      exporter.setPricePerPack(pricePerPack);
    }

  }

  public interface Exporter {
    void setId(UUID id);

    void setOrderableId(UUID productId);

    void setOrderableName(String productName);

    void setOrderableCode(Code productCode);

    void setOrderablePackSize(Long packSize);

    void setOrderableDisplayCategoryId(UUID orderableDisplayCategoryId);

    void setOrderableCategoryDisplayName(String orderableCategoryDisplayName);

    void setOrderableCategoryDisplayOrder(int orderableCategoryDisplayOrder);

    void setActive(boolean active);

    void setFullSupply(boolean fullSupply);

    void setDisplayOrder(int displayOrder);

    void setDosesPerPatient(Integer dosesPerPatient);

    void setPricePerPack(Money pricePerPack);
  }

  public interface Importer {
    UUID getId();

    String getOrderableName();

    Code getOrderableCode();

    Long getOrderablePackSize();

    UUID getOrderableDisplayCategoryId();

    String getOrderableCategoryDisplayName();

    int getOrderableCategoryDisplayOrder();

    boolean isActive();

    boolean isFullSupply();

    int getDisplayOrder();

    Integer getDosesPerPatient();

    Money getPricePerPack();
  }
}

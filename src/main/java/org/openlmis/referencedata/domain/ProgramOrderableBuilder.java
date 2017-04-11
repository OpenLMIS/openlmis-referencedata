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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.serializer.MoneyDeserializer;
import org.openlmis.referencedata.util.messagekeys.ProgramOrderableBuilderMessageKeys;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder of {@link ProgramOrderable}'s intended for use in deserialization.  This is a standard
 * builder pattern, however it requires that {@link #setProgramRepository(ProgramRepository)} is
 * called with a {@link ProgramRepository} so that it may lookup a Program's UUID and convert
 * it to {@link Program} in order to build a {@link ProgramOrderable}.
 */
public class ProgramOrderableBuilder {

  private ProgramRepository programRepo;
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepo;

  private UUID programId;
  private Integer dosesPerPatient;
  private boolean active;
  private UUID orderableDisplayCategoryId;
  private boolean fullSupply;
  private int displayOrder;
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money pricePerPack;

  private ProgramOrderableBuilder() {
    this.dosesPerPatient = null;
    this.active = true;
    this.orderableDisplayCategoryId = null;
    this.fullSupply = false;
    this.displayOrder = 0;
  }

  /**
   * Creates a new builder with the given program id.
   * @param programId a persistent program id that the
   * {@link #setProgramRepository(ProgramRepository)} will find.
   */
  public ProgramOrderableBuilder(UUID programId) {
    this();
    this.programId = Objects.requireNonNull(programId);
  }

  public ProgramOrderableBuilder setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public ProgramOrderableBuilder setOrderableDisplayCategoryId(UUID orderableDisplayCategoryId) {
    this.orderableDisplayCategoryId = orderableDisplayCategoryId;
    return this;
  }

  public ProgramOrderableBuilder setDosesPerPatient(Integer dosesPerPatient) {
    this.dosesPerPatient = dosesPerPatient;
    return this;
  }

  public ProgramOrderableBuilder setActive(boolean active) {
    this.active = active;
    return this;
  }

  public ProgramOrderableBuilder setProductId(UUID orderableDisplayCategoryId) {
    this.orderableDisplayCategoryId = orderableDisplayCategoryId;
    return this;
  }

  public ProgramOrderableBuilder setFullSupply(boolean fullSupply) {
    this.fullSupply = fullSupply;
    return this;
  }

  public ProgramOrderableBuilder setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  public ProgramOrderableBuilder setPricePerPack(Money pricePerPack) {
    this.pricePerPack = pricePerPack;
    return this;
  }

  public final void setProgramRepository(ProgramRepository repository) {
    this.programRepo = repository;
  }

  public final void setOrderableDisplayCategoryRepository(
      OrderableDisplayCategoryRepository repository) {
    this.orderableDisplayCategoryRepo = repository;
  }

  /**
   * Builds a new (non-persisted) {@link ProgramOrderable}.
   * This will build a program orderable that is ready for being persisted (or updating a
   * pre-persisted entity), using the UUID's given in
   * this builder by resolving them using the provided repository.
   * @param orderable the orderable for which we're building this ProgramOrderable.
   * @return a new ProgramOrderable ready for persisting.
   * @throws ValidationMessageException if {@link #setProgramRepository(ProgramRepository)}
   *     or {@link #setOrderableDisplayCategoryRepository(OrderableDisplayCategoryRepository)}
   *     wasn't called previously with a non-null repository.
   */
  public ProgramOrderable createProgramOrderable(Orderable orderable) {
    Objects.requireNonNull(programRepo,
        ProgramOrderableBuilderMessageKeys.ERROR_PROGRAM_REPOSITORY_NULL);
    Objects.requireNonNull(orderableDisplayCategoryRepo,
        ProgramOrderableBuilderMessageKeys.ERROR_ORDERABLE_DISPLAY_CATEGORY_REPOSITORY_NULL);
    Objects.requireNonNull(orderable, ProgramOrderableBuilderMessageKeys.ERROR_PRODUCT_NULL);

    Program storedProgram = programRepo.findOne(programId);
    OrderableDisplayCategory storedProdCategory = orderableDisplayCategoryRepo.findOne(
        orderableDisplayCategoryId);
    return ProgramOrderable.createNew(storedProgram, storedProdCategory, orderable, dosesPerPatient,
        active, fullSupply, displayOrder, pricePerPack,
        CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE)
    );
  }
}
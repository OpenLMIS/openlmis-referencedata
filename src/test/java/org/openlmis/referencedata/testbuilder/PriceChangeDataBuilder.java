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

package org.openlmis.referencedata.testbuilder;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.domain.PriceChange;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.User;

public class PriceChangeDataBuilder {

  private UUID id;
  private ProgramOrderable programOrderable;
  private Money price;
  private User author;
  private ZonedDateTime occurredDate;

  /**
   * Returns instance of {@link PriceChangeDataBuilder} with sample data.
   */
  public PriceChangeDataBuilder() {
    id = UUID.randomUUID();
    programOrderable = new ProgramOrderableDataBuilder().build();
    price = Money.of(CurrencyUnit.of("USD"), 0);
    author = new UserDataBuilder().build();
    occurredDate = ZonedDateTime.now();
  }

  /**
   * Builds instance of {@link PriceChange}.
   */
  public PriceChange build() {
    PriceChange priceChange = buildAsNew();
    priceChange.setId(id);

    return priceChange;
  }

  /**
   * Builds instance of {@link PriceChange} without id field.
   */
  public PriceChange buildAsNew() {
    return new PriceChange(programOrderable, price, author, occurredDate);
  }

  public PriceChangeDataBuilder withProgramOrderable(ProgramOrderable programOrderable) {
    this.programOrderable = programOrderable;
    return this;
  }

  public PriceChangeDataBuilder withPrice(Money price) {
    this.price = price;
    return this;
  }

  public PriceChangeDataBuilder withAuthor(User author) {
    this.author = author;
    return this;
  }

  public PriceChangeDataBuilder withOccurredDate(ZonedDateTime occurredDate) {
    this.occurredDate = occurredDate;
    return this;
  }

}

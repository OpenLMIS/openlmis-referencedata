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

import java.time.ZonedDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.money.Money;

@Getter
@Setter
@Entity
@Table(name = "price_changes")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PriceChange extends BaseEntity {

  @ManyToOne(cascade = {CascadeType.ALL})
  @JoinColumn(name = "programOrderableId")
  private ProgramOrderable programOrderable;

  @Type(type = "org.openlmis.referencedata.util.CustomSingleColumnMoneyUserType")
  private Money price;

  @ManyToOne
  @JoinColumn(name = "authorId", nullable = false)
  private User author;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private ZonedDateTime occurredDate;

  /**
   * Creates new instance based on data from {@link PriceChange.Importer}.
   *
   * @param importer instance of {@link PriceChange.Importer}
   * @return new instance of PriceChange.
   */
  public static PriceChange newInstance(PriceChange.Importer importer, User author) {
    PriceChange priceChange = new PriceChange();
    priceChange.setPrice(importer.getPrice());
    priceChange.setOccurredDate(importer.getOccurredDate());
    priceChange.setAuthor(author);

    return priceChange;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(PriceChange.Exporter exporter) {
    exporter.setPrice(price);
    exporter.setAuthor(author);
    exporter.setOccurredDate(occurredDate);
  }

  public interface Exporter {

    void setPrice(Money price);

    void setAuthor(User author);

    void setOccurredDate(ZonedDateTime occurredDate);

  }

  public interface Importer {

    Money getPrice();

    ZonedDateTime getOccurredDate();

  }

}

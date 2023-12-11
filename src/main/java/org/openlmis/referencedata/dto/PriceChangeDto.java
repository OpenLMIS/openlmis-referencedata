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

package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.money.Money;
import org.openlmis.referencedata.domain.PriceChange;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.serializer.MoneyDeserializer;
import org.openlmis.referencedata.serializer.MoneySerializer;
import org.openlmis.referencedata.web.BaseController;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public final class PriceChangeDto extends BaseDto implements PriceChange.Exporter,
    PriceChange.Importer {

  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money price;
  private ZonedDateTime occurredDate;
  private UserObjectReferenceDto author;

  /**
   * Create new List containing PriceChangeDto based on given a set of {@link PriceChange}.
   *
   * @param priceChanges Price changes.
   * @return a list containing dtos for all price changes.
   */
  public static List<PriceChangeDto> newInstance(List<PriceChange> priceChanges) {
    if (priceChanges == null) {
      return Collections.emptyList();
    }
    return priceChanges.stream()
        .map(PriceChangeDto::newInstance)
        .collect(Collectors.toList());
  }

  /**
   * Create new PriceChangeDto based on a given {@link PriceChange}.
   *
   * @param priceChange Price change.
   * @return a price change object converted to dto.
   */
  public static PriceChangeDto newInstance(PriceChange priceChange) {
    PriceChangeDto dto = new PriceChangeDto();
    priceChange.export(dto);
    return dto;
  }

  @Override
  public void setAuthor(User author) {
    this.author = new UserObjectReferenceDto("referencedata",
        BaseController.API_PATH + "/users", author.getId());
    this.author.setFirstName(author.getFirstName());
    this.author.setLastName(author.getLastName());
  }

}

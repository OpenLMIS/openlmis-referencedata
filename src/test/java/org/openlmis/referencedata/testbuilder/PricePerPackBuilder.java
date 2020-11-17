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

import java.util.HashSet;
import java.util.Set;

import org.joda.money.Money;
import org.openlmis.referencedata.dto.ProgramOrderableDto;

public class PricePerPackBuilder {

  private Set<ProgramOrderableDto> programs;

  /**
   * PricePerPackBuilder constructor.
   */
  public PricePerPackBuilder() {
    ProgramOrderableDto programOrderableDto = new ProgramOrderableDto();
    Money pricePerPack = Money.parse("USD -23.87");
    programOrderableDto.setPricePerPack(pricePerPack);
    this.programs = new HashSet<>();
    programs.add(programOrderableDto);
  }

  /**
   * Building programs set with pricperpack value.
   * @return
   */
  public Set<ProgramOrderableDto> build() {
    return this.programs;
  }
}

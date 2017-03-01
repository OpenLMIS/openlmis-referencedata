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

package org.openlmis.referencedata.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.ValidationMessageException;

import java.time.LocalDate;

public class ProcessingPeriodDtoComparatorTest {

  private ProcessingPeriodDto firstPeriod;
  private ProcessingPeriodDto secondPeriod;
  private ProcessingPeriodDtoComparator comparator;

  @Before
  public void setUp() {
    firstPeriod = new ProcessingPeriodDto();
    secondPeriod = new ProcessingPeriodDto();
  }

  @Test
  public void shouldReturnNegativeIntWhenFirstStartDateBeforeSecond() {
    comparator = new ProcessingPeriodDtoComparator("startDate");

    firstPeriod.setStartDate(LocalDate.of(2016, 1, 1));
    secondPeriod.setStartDate(LocalDate.of(2016, 2, 1));

    assertThat(comparator.compare(firstPeriod, secondPeriod), lessThan(0));
  }

  @Test
  public void shouldReturnPositiveIntWhenFirstStartDateAfterSecond() {
    comparator = new ProcessingPeriodDtoComparator("startDate");

    firstPeriod.setStartDate(LocalDate.of(2016, 2, 1));
    secondPeriod.setStartDate(LocalDate.of(2016, 1, 1));

    assertThat(comparator.compare(firstPeriod, secondPeriod), greaterThan(0));
  }

  @Test
  public void shouldReturnZeroWhenFirstStartDateEqualsSecond() {
    comparator = new ProcessingPeriodDtoComparator("startDate");

    firstPeriod.setStartDate(LocalDate.of(2016, 1, 1));
    secondPeriod.setStartDate(LocalDate.of(2016, 1, 1));

    assertThat(comparator.compare(firstPeriod, secondPeriod), is(0));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenInvalidSortingColumn() {
    comparator = new ProcessingPeriodDtoComparator("");

    firstPeriod.setStartDate(LocalDate.of(2016, 1, 1));
    secondPeriod.setStartDate(LocalDate.of(2016, 1, 1));

    comparator.compare(firstPeriod, secondPeriod);
  }
}
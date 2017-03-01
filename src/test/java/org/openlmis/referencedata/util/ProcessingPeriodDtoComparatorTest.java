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
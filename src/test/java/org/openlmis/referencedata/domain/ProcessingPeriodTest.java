package org.openlmis.referencedata.domain;


import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class ProcessingPeriodTest {

  private static ProcessingSchedule schedule;
  private static LocalDate startDate;
  private static LocalDate endDate;
  private static final String NAME = "name";

  @Before
  public void initialize() {
    schedule = new ProcessingSchedule("123", NAME);
  }


  @Test
  public void shouldCalculateCorrectlyWhenTwoMonths() {
    startDate = LocalDate.of(2016, 10, 11);
    endDate = LocalDate.of(2016, 11, 30);

    assertTrue(isDurationAsExpected(startDate, endDate, 2));
  }

  @Test
  public void shouldCalculateCorrectlyWhenLessThanOneMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 1, 20);

    assertTrue(isDurationAsExpected(startDate, endDate, 1));
  }

  @Test
  public void shouldCalculateCorrectlyWhenOneMonthAndAHalf() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 2, 16);

    assertTrue(isDurationAsExpected(startDate, endDate, 2));
  }

  @Test
  public void shouldCalculateCorrectlyWhenLastDayOfMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 3, 31);

    assertTrue(isDurationAsExpected(startDate, endDate, 3));
  }

  @Test
  public void shouldCalculateCorrectlyWhenMonthAndOneDay() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 2, 2);

    assertTrue(isDurationAsExpected(startDate, endDate, 1));
  }

  @Test
  public void shouldCalculateCorrectlyWhenLastDayOfFirstMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 1, 31);

    assertTrue(isDurationAsExpected(startDate, endDate, 1));
  }

  @Test
  public void shouldCalculateCorrectlyWhenLessThanTwoWeeks() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 1, 10);

    assertTrue(isDurationAsExpected(startDate,  endDate, 1));
  }

  @Test
  public void shouldCalculateCorrectlyWhenMoreThanOneYear() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2017, 2, 2);

    assertTrue(isDurationAsExpected(startDate, endDate, 13));
  }

  private boolean isDurationAsExpected(LocalDate start, LocalDate end, int expected) {
    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, start, end);
    return period.getDurationInMonths() == expected;
  }
}

package org.openlmis.referencedata.domain;


import static org.junit.Assert.assertEquals;

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
    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(2, period.getLengthOfInMonths());
  }

  @Test
  public void shouldCalculateCorrectlyWhenLessThanOneMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 1, 20);

    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(1, period.getLengthOfInMonths());
  }

  @Test
  public void shouldCalculateCorrectlyWhenOneMonthAndAHalf() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 2, 16);

    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(2, period.getLengthOfInMonths());
  }

  @Test
  public void shouldCalculateCorrectlyWhenLastDayOfMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 3, 31);

    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(3, period.getLengthOfInMonths());
  }

  @Test
  public void shouldCalculateCorrectlyWhenMonthAndOneDay() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 2, 2);

    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(1, period.getLengthOfInMonths());
  }

  @Test
  public void shouldCalculateCorrectlyWhenLastDayOfFirstMonth() {
    startDate = LocalDate.of(2016, 1, 1);
    endDate = LocalDate.of(2016, 1, 31);

    ProcessingPeriod period = ProcessingPeriod.newPeriod(NAME, schedule, startDate, endDate);

    assertEquals(1, period.getLengthOfInMonths());
  }
}

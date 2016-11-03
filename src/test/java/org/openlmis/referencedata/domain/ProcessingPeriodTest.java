package org.openlmis.referencedata.domain;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

public class ProcessingPeriodTest {

  private static ProcessingSchedule schedule;
  private static LocalDate startDate;
  private static LocalDate endDate;

  {
    schedule = new ProcessingSchedule("123", "name");
    startDate = LocalDate.of(2016, 10, 11);
    endDate = LocalDate.of(2016, 11, 30);
  }


  @Test
  public void shouldCalculateLengthCorrectly() {
    ProcessingPeriod period = ProcessingPeriod.newPeriod("name", schedule, startDate, endDate);

    assertEquals("2", period.getLengthOf());
  }
}

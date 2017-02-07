package org.openlmis.referencedata.repository;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessingScheduleRepositoryIntegrationTest
      extends BaseCrudRepositoryIntegrationTest<ProcessingSchedule> {

  @Autowired
  ProcessingScheduleRepository repository;

  @Before
  public void setUp() {
    repository.save(getExampleSchedule());
  }

  @Override
  ProcessingScheduleRepository getRepository() {
    return this.repository;
  }

  @Override
  ProcessingSchedule generateInstance() {
    return getExampleSchedule();
  }

  @Test
  public void testGetAllSchedules() {
    repository.save(getExampleSchedule());
    Iterable<ProcessingSchedule> result = repository.findAll();
    int size = Lists.newArrayList(result).size();
    Assert.assertEquals(7, size);
  }

  @Test
  public void testScheduleEdit() {
    Iterable<ProcessingSchedule> iterable = repository.findAll();
    ProcessingSchedule scheduleFromRepo = iterable.iterator().next();
    String newDescription = "New test description babe";
    Assert.assertNotEquals(newDescription, scheduleFromRepo.getDescription());

    scheduleFromRepo.setDescription(newDescription);
    repository.save(scheduleFromRepo);
    Assert.assertEquals(newDescription, scheduleFromRepo.getDescription());
  }

  private ProcessingSchedule getExampleSchedule() {
    int instanceNumber = this.getNextInstanceNumber();
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode("code" + instanceNumber);
    schedule.setName("schedule#" + instanceNumber);
    schedule.setDescription("Test schedule");
    return schedule;
  }
}

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

package org.openlmis.referencedata.repository;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    assertEquals(2, size);
  }

  @Test
  public void testScheduleEdit() {
    Iterable<ProcessingSchedule> iterable = repository.findAll();
    ProcessingSchedule scheduleFromRepo = iterable.iterator().next();
    String newDescription = "New test description babe";
    Assert.assertNotEquals(newDescription, scheduleFromRepo.getDescription());

    scheduleFromRepo.setDescription(newDescription);
    repository.save(scheduleFromRepo);
    ZonedDateTime savingDateTime = scheduleFromRepo.getModifiedDate();
    iterable = repository.findAll();
    scheduleFromRepo = iterable.iterator().next();
    assertTrue(savingDateTime.isBefore(scheduleFromRepo.getModifiedDate()));
    assertEquals(newDescription, scheduleFromRepo.getDescription());
  }

  @Test
  public void shouldCheckIfPeriodExistsByCode() {
    ProcessingSchedule schedule = generateInstance();
    assertFalse(repository.existsByCode(schedule.getCode()));

    schedule = repository.save(schedule);
    assertTrue(repository.existsByCode(schedule.getCode()));
  }

  @Test
  public void shouldFindByNameAndSchedule() {
    ProcessingSchedule schedule = generateInstance();
    assertEquals(null, repository.findByCode(schedule.getCode()));

    schedule = repository.save(schedule);
    assertEquals(schedule.getId(), repository.findByCode(schedule.getCode()).getId());
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

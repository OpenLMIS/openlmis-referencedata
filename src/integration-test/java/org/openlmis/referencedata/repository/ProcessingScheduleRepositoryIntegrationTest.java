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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ProcessingScheduleRepositoryIntegrationTest
      extends BaseCrudRepositoryIntegrationTest<ProcessingSchedule> {

  @Autowired
  private ProcessingScheduleRepository repository;

  private ProcessingSchedule schedule;

  @Before
  public void setUp() {
    schedule = generateInstance();
    repository.save(schedule);
  }

  @Override
  ProcessingScheduleRepository getRepository() {
    return this.repository;
  }

  @Override
  ProcessingSchedule generateInstance() {
    return new ProcessingScheduleDataBuilder().buildWithoutId();
  }

  @Test
  public void testGetAllSchedules() {
    repository.save(generateInstance());
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
  public void shouldGetScheduleByCode() {
    final String code1 = "code1";
    final String code2 = "code2";
    final String code3 = "code3";
    repository.save(new ProcessingScheduleDataBuilder().withCode(code1).buildWithoutId());
    repository.save(new ProcessingScheduleDataBuilder().withCode(code2).buildWithoutId());
    repository.save(new ProcessingScheduleDataBuilder().withCode(code3).buildWithoutId());

    Optional<ProcessingSchedule> actual1 = repository.findOneByCode(Code.code(code1));
    Optional<ProcessingSchedule> actual2 = repository.findOneByCode(Code.code(code2));
    Optional<ProcessingSchedule> actual3 = repository.findOneByCode(Code.code(code3));

    assertTrue(actual1.isPresent());
    assertTrue(actual2.isPresent());
    assertTrue(actual3.isPresent());
    assertEquals(code1, actual1.get().getCode().toString());
    assertEquals(code2, actual2.get().getCode().toString());
    assertEquals(code3, actual3.get().getCode().toString());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldThrowExceptionIfCodeIsDuplicatedCaseInsensitive() {
    ProcessingSchedule scheduleLowerCase = new ProcessingScheduleDataBuilder()
        .withCode("abc")
        .buildWithoutId();
    repository.saveAndFlush(scheduleLowerCase);

    ProcessingSchedule scheduleUpperCase = new ProcessingScheduleDataBuilder()
        .withCode("ABC")
        .buildWithoutId();
    repository.saveAndFlush(scheduleUpperCase);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldThrowExceptionIfNameIsDuplicatedCaseInsensitive() {
    ProcessingSchedule scheduleLowerCase = new ProcessingScheduleDataBuilder()
        .withName("some-name")
        .buildWithoutId();
    repository.saveAndFlush(scheduleLowerCase);

    ProcessingSchedule scheduleUpperCase = new ProcessingScheduleDataBuilder()
        .withName("SOME-NAME")
        .buildWithoutId();
    repository.saveAndFlush(scheduleUpperCase);
  }

  @Test
  public void shouldFindByCode() {
    ProcessingSchedule entity = generateInstance();
    repository.save(entity);

    Optional<ProcessingSchedule> db = repository.findOneByCode(entity.getCode());
    assertThat(db.isPresent(), is(true));
    assertThat(db.get(), is(entity));
  }
}

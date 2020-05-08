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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingPeriodDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.RequisitionGroupDataBuilder;
import org.openlmis.referencedata.testbuilder.RequisitionGroupProgramScheduleDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingPeriodRepositoryIntegrationTest
      extends BaseCrudRepositoryIntegrationTest<ProcessingPeriod> {

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private RequisitionGroupProgramScheduleRepository programScheduleRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  private Program program;
  private ProcessingSchedule schedule;
  private Facility facility;

  private PageRequest pageable = PageRequest.of(0, 10);

  private ProcessingPeriod period1;
  private ProcessingPeriod period2;
  private ProcessingPeriod period3;

  ProcessingPeriodRepository getRepository() {
    return this.periodRepository;
  }

  @Before
  public void setUp() {
    schedule = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule);

    period1 = periodRepository.save(generateInstance());
    period2 = periodRepository.save(
        generateInstance(period1.getStartDate().plusMonths(1), period1.getEndDate().plusMonths(1)));
    period3 = periodRepository.save(
        generateInstance(period2.getStartDate().plusMonths(1), period2.getEndDate().plusMonths(1)));

    RequisitionGroupProgramSchedule entity = generateRequisitionGroupProgramSchedule();
    programScheduleRepository.save(entity);
  }

  @Test
  public void shouldFindPeriodsStartDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(null, null, null, period2.getEndDate(), null, null, pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period2, period3));
  }

  @Test
  public void shouldFindPeriodsByEndDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(null, null, null, null, period2.getStartDate(), null, pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2));
  }

  @Test
  public void shouldFindPeriodsByStartDateAndEndDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(null, null, null, period1.getEndDate(), period2.getStartDate(), null, pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2));
  }

  @Test
  public void shouldFindPeriodsByIds() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(schedule.getId(), null, null, null, null,
            asSet(period1.getId(), period2.getId(), period3.getId()), pageable);

    assertEquals(3, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2, period3));
  }

  @Test
  public void shouldFindPeriodsBySchedulePage() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    Page<ProcessingPeriod> periods = periodRepository.search(schedule.getId(), null, null,
        null, null, null, pageable);
    assertEquals(3, periods.getTotalElements());
    assertEquals(schedule, periods.getContent().get(0).getProcessingSchedule());
    assertEquals(schedule, periods.getContent().get(1).getProcessingSchedule());
    assertEquals(schedule, periods.getContent().get(2).getProcessingSchedule());

    periods = periodRepository.search(schedule2.getId(), null, null,
        null, null, null, pageable);
    assertEquals(1, periods.getTotalElements());
    assertEquals(schedule2, periods.getContent().get(0).getProcessingSchedule());
  }

  @Test
  public void shouldFindPeriodsByScheduleAndStartDateAndEndDateAndIds() {
    ProcessingSchedule newSchedule = scheduleRepository
        .save(new ProcessingScheduleDataBuilder().buildWithoutId());

    ProcessingPeriod period4 = periodRepository.save(generateInstance(newSchedule));
    ProcessingPeriod period5 = periodRepository.save(generateInstance(newSchedule,
        period1.getStartDate().plusMonths(1), period1.getEndDate().plusMonths(1)));
    periodRepository.save(generateInstance(newSchedule,
        period2.getStartDate().plusMonths(1), period2.getEndDate().plusMonths(1)));

    Page<ProcessingPeriod> periods = periodRepository
        .search(newSchedule.getId(), null, null, period4.getEndDate(), period5.getStartDate(),
            asSet(period1.getId(), period2.getId(), period4.getId(), period5.getId()),
            pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period4, period5));
  }

  @Test
  public void shouldSortByStartDateDesc() {
    pageable = PageRequest.of(0, 10, Direction.DESC, "startDate");

    Page<ProcessingPeriod> page = periodRepository.search(schedule.getId(), null, null, null,
        null, null, pageable);
    List<ProcessingPeriod> content = page.getContent();

    assertThat(content, hasSize(3));
    assertThat(content, contains(period3, period2, period1));
  }

  @Test
  public void shouldFindPeriodsByScheduleList() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    List<ProcessingPeriod> periods = periodRepository
        .findByProcessingScheduleOrderByEndDate(schedule);
    assertEquals(3, periods.size());
    assertEquals(schedule, periods.get(0).getProcessingSchedule());
    assertEquals(schedule, periods.get(1).getProcessingSchedule());
    assertEquals(schedule, periods.get(2).getProcessingSchedule());

    periods = periodRepository.findByProcessingScheduleOrderByEndDate(schedule2);
    assertEquals(1, periods.size());
    assertEquals(schedule2, periods.get(0).getProcessingSchedule());
  }

  @Test
  public void shouldPeriodsFoundByScheduleListBeOrderedByEndDate() {
    // Triggering update to change the default order
    period2.setName("custom name");
    periodRepository.save(period2);

    List<ProcessingPeriod> periods = periodRepository
        .findByProcessingScheduleOrderByEndDate(schedule);

    assertEquals(3, periods.size());
    assertTrue(periods.get(0).getEndDate().isBefore(periods.get(1).getEndDate()));
    assertTrue(periods.get(1).getEndDate().isBefore(periods.get(2).getEndDate()));
  }

  @Test
  public void shouldFindPeriodsByNameAndSchedule() {
    period3 = new ProcessingPeriodDataBuilder()
        .withName("some-other-name")
        .withSchedule(schedule)
        .buildAsNew();
    periodRepository.save(period3);

    Optional<ProcessingPeriod> result = periodRepository
        .findOneByNameAndProcessingSchedule(period1.getName(), schedule);
    assertTrue(result.isPresent());
    assertEquals(period1, result.get());
  }

  @Test
  public void shouldFindPeriodsByProgramAndFacility() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    Page<ProcessingPeriod> periods = periodRepository
        .search(null, program.getId(), facility.getId(), null, null, null, pageable);

    assertEquals(3, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2, period3));
  }

  @Test
  public void shouldFindPeriodsByProgram() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    Page<ProcessingPeriod> periods = periodRepository
        .search(null, program.getId(), null, null, null, null, pageable);

    assertEquals(3, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2, period3));
  }

  ProcessingPeriod generateInstance() {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(ProcessingSchedule processingSchedule) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(processingSchedule)
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .withStartDate(null != startDate ? startDate : LocalDate.now())
        .withEndDate(null != endDate ? endDate : LocalDate.now().plusMonths(1))
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(ProcessingSchedule processingSchedule,
      LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(processingSchedule)
        .withStartDate(null != startDate ? startDate : LocalDate.now())
        .withEndDate(null != endDate ? endDate : LocalDate.now().plusMonths(1))
        .buildAsNew();
  }

  RequisitionGroupProgramSchedule generateRequisitionGroupProgramSchedule() {
    program = new ProgramDataBuilder().build();
    programRepository.save(program);

    FacilityType facilityType = new FacilityTypeDataBuilder().build();
    facilityTypeRepository.save(facilityType);

    GeographicLevel level = new GeographicLevelDataBuilder().build();
    geographicLevelRepository.save(level);

    GeographicZone geographicZone = new GeographicZoneDataBuilder().withLevel(level).buildAsNew();
    geographicZoneRepository.save(geographicZone);

    facility = new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withoutOperator()
        .withType(facilityType)
        .buildAsNew();
    facilityRepository.save(facility);

    SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();
    supervisoryNodeRepository.save(supervisoryNode);

    RequisitionGroup requisitionGroup = new RequisitionGroupDataBuilder()
        .withMemberFacility(facility)
        .withSupervisoryNode(supervisoryNode)
        .build();
    requisitionGroupRepository.save(requisitionGroup);

    return new RequisitionGroupProgramScheduleDataBuilder()
        .withProgram(program)
        .withRequisitionGroup(requisitionGroup)
        .withSchedule(schedule)
        .withDropOffFacility(facility)
        .buildAsNew();
  }
}

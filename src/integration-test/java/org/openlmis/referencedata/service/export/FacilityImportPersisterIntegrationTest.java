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

package org.openlmis.referencedata.service.export;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.testbuilder.FacilityOperatorDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.util.TransactionUtils;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataImportService.class})
@ActiveProfiles({"test", "test-run"})
public class FacilityImportPersisterIntegrationTest {
  @Autowired private FacilityTypeRepository facilityTypeRepository;
  @Autowired private GeographicLevelRepository geographicLevelRepository;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private FacilityOperatorRepository facilityOperatorRepository;
  @Autowired private FacilityImportPersister facilityImportPersister;
  @Mock private TransactionUtils transactionUtils;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(
        facilityImportPersister, "importExecutorService", MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(
        facilityImportPersister, "transactionUtils", transactionUtils);
    when(transactionUtils.runInOwnTransaction(any(Supplier.class)))
        .thenAnswer(invocation -> ((Supplier) invocation.getArgument(0)).get());
  }

  @Test
  @Transactional
  public void shouldImportFacility() throws IOException, InterruptedException {
    facilityTypeRepository.save(new FacilityTypeDataBuilder().withCode("TestType").buildAsNew());
    final GeographicLevel geographicLevel =
        geographicLevelRepository.save(new GeographicLevelDataBuilder().buildAsNew());
    geographicZoneRepository.save(
        new GeographicZoneDataBuilder()
            .withCode("TestZone")
            .withLevel(geographicLevel)
            .buildAsNew());
    facilityOperatorRepository.save(
        new FacilityOperatorDataBuilder().withCode("TestOperator").buildAsNew());

    final ImportResponseDto.ImportDetails facilityDtos =
        facilityImportPersister.processAndPersist(
            new ClassPathResource("/FacilityImportPersisterTest/facility.csv").getInputStream(),
            mock(Profiler.class));

    assertEquals(Integer.valueOf(1), facilityDtos.getSuccessfulEntriesCount());
  }
}

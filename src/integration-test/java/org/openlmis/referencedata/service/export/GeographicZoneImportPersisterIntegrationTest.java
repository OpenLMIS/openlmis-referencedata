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
import java.util.List;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.GeographicZoneDto;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
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
public class GeographicZoneImportPersisterIntegrationTest {

  @Autowired private GeographicLevelRepository geographicLevelRepository;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private GeographicZonesImportPersister geographicZonesImportPersister;

  @Mock
  private TransactionUtils transactionUtils;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(
        geographicZonesImportPersister,
        "importExecutorService",
        MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(
            geographicZonesImportPersister, "transactionUtils", transactionUtils);
    when(transactionUtils.runInOwnTransaction(any(Supplier.class)))
        .thenAnswer(invocation -> ((Supplier) invocation.getArgument(0)).get());
  }

  @Test
  @Transactional
  public void shouldUpdateCatchmentPopulation() throws IOException, InterruptedException {
    final GeographicLevel geographicLevel =
        geographicLevelRepository.save(new GeographicLevelDataBuilder().buildAsNew());
    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel).withCode("ZONE").buildAsNew();

    geographicZoneRepository.save(geographicZone);

    final List<GeographicZoneDto> geographicZoneDtos =
        geographicZonesImportPersister.processAndPersist(
            new ClassPathResource("/GeographicZoneImportPersisterTest/geographicZone.csv")
                .getInputStream(),
            mock(Profiler.class));

    assertEquals(1, geographicZoneDtos.size());
    assertEquals(Integer.valueOf(200), geographicZoneDtos.get(0).getCatchmentPopulation());
  }
}

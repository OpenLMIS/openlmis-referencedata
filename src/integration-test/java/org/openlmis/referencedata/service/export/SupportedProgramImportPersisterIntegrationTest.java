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

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataImportService.class})
@ActiveProfiles({"test", "test-run"})
public class SupportedProgramImportPersisterIntegrationTest {
  @Autowired private GeographicLevelRepository geographicLevelRepository;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private FacilityTypeRepository facilityTypeRepository;
  @Autowired private ProgramRepository programRepository;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private SupportedProgramImportPersister supportedProgramImportPersister;

  @Test
  @Transactional
  public void shouldImportSupportedProgram() throws IOException {
    final GeographicLevel geographicLevel =
        geographicLevelRepository.save(new GeographicLevelDataBuilder().buildAsNew());
    final GeographicZone geographicZone =
        geographicZoneRepository.save(
            new GeographicZoneDataBuilder()
                .withCode("TestZone")
                .withLevel(geographicLevel)
                .buildAsNew());
    final FacilityType facilityType =
        facilityTypeRepository.save(
            new FacilityTypeDataBuilder().withCode("TestType").buildAsNew());
    facilityRepository.save(
        new FacilityDataBuilder()
            .withCode("TestFacility1")
            .withGeographicZone(geographicZone)
            .withType(facilityType)
            .withOperator(null)
            .buildAsNew());
    programRepository.save(new ProgramDataBuilder().withCode("TestProgram1").buildAsNew());

    final List<SupportedProgramDto> supportedProgramDtos =
        supportedProgramImportPersister.processAndPersist(
            new ClassPathResource(
                    "/SupportedProgramImportPersisterIntegrationTest/supportedProgram.csv")
                .getInputStream());

    assertEquals(1, supportedProgramDtos.size());
  }
}

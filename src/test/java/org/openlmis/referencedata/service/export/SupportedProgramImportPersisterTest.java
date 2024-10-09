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

import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.SupportedProgramPrimaryKey;
import org.openlmis.referencedata.dto.SupportedProgramCsvModel;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupportedProgramRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupportedProgramDataBuilder;
import org.openlmis.referencedata.util.FileHelper;

@RunWith(MockitoJUnitRunner.class)
public class SupportedProgramImportPersisterTest {
  @Mock private FileHelper fileHelper;
  @Mock private FacilityRepository facilityRepository;
  @Mock private ProgramRepository programRepository;
  @Mock private SupportedProgramRepository supportedProgramRepository;

  @InjectMocks private SupportedProgramImportPersister supportedProgramPersister;

  private Facility facility;
  private Program program;
  private SupportedProgram supportedProgram;
  private InputStream dataStream = mock(InputStream.class);

  @Before
  public void setup() {
    facility = new FacilityDataBuilder().build();
    program = new ProgramDataBuilder().build();
    supportedProgram =
        new SupportedProgramDataBuilder().withFacility(facility).withProgram(program).build();

    final SupportedProgramCsvModel csvModel =
        new SupportedProgramCsvModel(
            program.getCode().toString(),
            facility.getCode(),
            supportedProgram.getActive(),
            supportedProgram.getLocallyFulfilled(),
            supportedProgram.getStartDate());

    when(fileHelper.readCsv(SupportedProgramCsvModel.class, dataStream))
        .thenReturn(singletonList(csvModel));
    when(facilityRepository.findByCode(facility.getCode())).thenReturn(of(facility));
    when(programRepository.findByCode(program.getCode())).thenReturn(program);
    when(supportedProgramRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<SupportedProgram> answer = new ArrayList<>();
              Iterable<?> supportedPrograms = invocation.getArgument(0);
              for (Object sp : supportedPrograms) {
                answer.add((SupportedProgram) sp);
              }
              return answer;
            });
  }

  @Test
  public void shouldCreateSupportedProgram() {
    // Given
    when(supportedProgramRepository.findById(new SupportedProgramPrimaryKey(facility, program)))
        .thenReturn(Optional.empty());

    // When
    List<SupportedProgramDto> result = supportedProgramPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(SupportedProgramCsvModel.class, dataStream);
    verify(supportedProgramRepository).saveAll(singletonList(supportedProgram));
  }

  @Test
  public void shouldUpdateSupportedProgram() {
    // Given
    when(supportedProgramRepository.findById(new SupportedProgramPrimaryKey(facility, program)))
        .thenReturn(of(supportedProgram));

    // When
    List<SupportedProgramDto> result = supportedProgramPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(SupportedProgramCsvModel.class, dataStream);
    verify(supportedProgramRepository).saveAll(singletonList(supportedProgram));
  }
}

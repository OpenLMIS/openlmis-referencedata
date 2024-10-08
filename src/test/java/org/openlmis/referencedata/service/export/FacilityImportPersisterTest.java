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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.service.FacilityBuilder;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.util.FileHelper;

@RunWith(MockitoJUnitRunner.class)
public class FacilityImportPersisterTest {
  private InputStream dataStream;
  private Facility facility;
  private FacilityDto dto;

  @Mock private FileHelper fileHelper;

  @Mock private FacilityRepository facilityRepository;

  @Mock private FacilityBuilder facilityBuilder;

  @InjectMocks private FacilityImportPersister facilityImportPersister;

  @Before
  public void setUp() {
    dataStream = mock(InputStream.class);
    facility = new FacilityDataBuilder().build();
    dto = FacilityDto.newInstance(facility);
  }

  @Test
  public void shouldCreateFacility() {
    // Given
    setupCommonMocks();
    when(facilityRepository.findByCode(facility.getCode())).thenReturn(Optional.empty());

    // When
    List<FacilityDto> result = facilityImportPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(FacilityDto.class, dataStream);
    verify(facilityRepository).saveAll(Collections.singletonList(facility));
  }

  @Test
  public void shouldUpdateFacility() {
    // Given
    setupCommonMocks();
    when(facilityRepository.findByCode(facility.getCode())).thenReturn(Optional.of(facility));

    // When
    List<FacilityDto> result = facilityImportPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(FacilityDto.class, dataStream);
    verify(facilityRepository).saveAll(Collections.singletonList(facility));
  }

  private void setupCommonMocks() {
    when(fileHelper.readCsv(FacilityDto.class, dataStream))
        .thenReturn(Collections.singletonList(dto));
    when(facilityBuilder.build(any(FacilityDto.class)))
        .thenAnswer(
            invocation -> {
              FacilityDto facilityDto = invocation.getArgument(0);
              Facility facility = new Facility();
              facility.updateFrom(facilityDto);
              return facility;
            });
    when(facilityRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<Facility> answer = new ArrayList<>();
              Iterable<?> facilities = invocation.getArgument(0);
              for (Object facility : facilities) {
                answer.add((Facility) facility);
              }
              return answer;
            });
  }
}

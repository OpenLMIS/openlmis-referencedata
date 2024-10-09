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
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.util.FileHelper;

@RunWith(MockitoJUnitRunner.class)
public class FacilityImportPersisterTest {
  private InputStream dataStream;
  private Facility facility;
  private FacilityDto dto;

  @Mock private FileHelper fileHelper;
  @Mock private FacilityRepository facilityRepository;
  @Mock private GeographicZoneRepository geographicZoneRepository;
  @Mock private FacilityTypeRepository facilityTypeRepository;
  @Mock private FacilityOperatorRepository facilityOperatorRepository;
  @InjectMocks private FacilityImportPersister facilityImportPersister;

  @Before
  public void setUp() {
    dataStream = mock(InputStream.class);
    facility = new FacilityDataBuilder().build();
    dto = FacilityDto.newInstance(facility);

    when(fileHelper.readCsv(FacilityDto.class, dataStream))
        .thenReturn(Collections.singletonList(dto));
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
    when(geographicZoneRepository.findByCode(facility.getGeographicZone().getCode()))
        .thenReturn(mock(GeographicZone.class));
    when(facilityTypeRepository.findOneByCode(facility.getType().getCode()))
        .thenReturn(mock(FacilityType.class));
    when(facilityOperatorRepository.findByCode(facility.getOperator().getCode()))
        .thenReturn(mock(FacilityOperator.class));
  }

  @Test
  public void shouldCreateFacility() {
    // Given
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
    when(facilityRepository.findByCode(facility.getCode())).thenReturn(Optional.of(facility));

    // When
    List<FacilityDto> result = facilityImportPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(FacilityDto.class, dataStream);
    verify(facilityRepository).saveAll(Collections.singletonList(facility));
  }
}

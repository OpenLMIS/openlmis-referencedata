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
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.OrderableBuilder;

@RunWith(MockitoJUnitRunner.class)
public class OrderableImportPersisterTest {

  private InputStream dataStream;
  private Orderable orderable;
  private OrderableDto dto;

  @Mock
  private FileHelper fileHelper;

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private OrderableBuilder orderableBuilder;

  @InjectMocks
  private OrderableImportPersister orderableImportPersister;

  @Before
  public void setUp() {
    dataStream = mock(InputStream.class);
    orderable = mock(Orderable.class);
    dto = OrderableDto.newInstance(orderable);
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistData() {
    // Given
    setupMocksForSuccess();

    // When
    List<OrderableDto> result = orderableImportPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(OrderableDto.class, dataStream);
    verify(orderableRepository).saveAll(any());
  }

  private void setupMocksForSuccess() {
    when(fileHelper.readCsv(OrderableDto.class, dataStream)).thenReturn(
        Collections.singletonList(dto));
    when(orderableRepository.findFirstByProductCodeOrderByIdentityVersionNumberDesc(
        any(Code.class))).thenReturn(orderable);
    when(orderableBuilder.newOrderable(any(OrderableDto.class), any(Orderable.class)))
        .thenReturn(orderable);
    when(orderableRepository.saveAll(any())).thenReturn(
        Collections.singletonList(orderable));
  }

}
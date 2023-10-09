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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.TradeItemCsvModel;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.openlmis.referencedata.util.FileHelper;

@RunWith(MockitoJUnitRunner.class)
public class TradeItemImportPersisterTest {

  private InputStream dataStream;
  private TradeItemCsvModel csvModel;
  private Orderable orderable;
  private String identifier;
  private Map<String, String> identifiers = new HashMap<>();
  private TradeItem tradeItem;

  @Mock
  private FileHelper fileHelper;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @Mock
  private OrderableRepository orderableRepository;

  @InjectMocks
  private TradeItemImportPersister tradeItemImportPersister;

  @Before
  public void setUp() {
    // Initialize mock objects
    dataStream = mock(InputStream.class);

    // Initialize objects
    identifier = UUID.randomUUID().toString();
    csvModel = new TradeItemCsvModel("code", "manufacturer");
    tradeItem = new TradeItemDataBuilder()
        .build();
    orderable = new OrderableDataBuilder()
        .withIdentifier("tradeItem", identifier)
        .build();

    // Set fields
    identifiers.put("tradeItem", identifier);
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistData() {
    // Given
    setupMocksForSuccess();

    // When
    List<OrderableDto> result = tradeItemImportPersister.processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(TradeItemCsvModel.class, dataStream);
    verify(tradeItemRepository).saveAll(any());
    verify(orderableRepository).saveAll(any());
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistDataWithNotExistingIdentifier() {
    // Given
    setupMocksForSuccess();
    orderable.setIdentifiers(null);

    // When
    Set<Orderable> result = tradeItemImportPersister
        .createOrUpdate(Collections.singletonList(csvModel));

    // Then
    assertEquals(1, result.size());
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistDataWithExistingIdentifier() {
    // Given
    setupMocksForSuccess();

    // When
    Set<Orderable> result = tradeItemImportPersister
        .createOrUpdate(Collections.singletonList(csvModel));

    // Then
    assertEquals(1, result.size());
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowErrorIfOrderableNotFound() {
    // Given
    List<TradeItemCsvModel> list = Arrays.asList(
        mock(TradeItemCsvModel.class), mock(TradeItemCsvModel.class)
    );
    when(orderableRepository.findFirstByProductCodeOrderByIdentityVersionNumberDesc(
        any(Code.class))).thenReturn(null);

    // When
    tradeItemImportPersister.createOrUpdate(list);
  }

  private void setupMocksForSuccess() {
    when(fileHelper.readCsv(TradeItemCsvModel.class, dataStream))
        .thenReturn(Collections.singletonList(csvModel));
    when(orderableRepository.findFirstByProductCodeOrderByIdentityVersionNumberDesc(
        any(Code.class))).thenReturn(orderable);
    when(tradeItemRepository.saveAll(any()))
        .thenReturn(Collections.singletonList(tradeItem));
    when(tradeItemRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(tradeItem));
    when(orderableRepository.saveAll(any())).thenReturn(Collections.singletonList(orderable));
  }

}
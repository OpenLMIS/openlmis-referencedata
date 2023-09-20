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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.dto.ProgramOrderableCsvModel;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.FileHelper;

@RunWith(MockitoJUnitRunner.class)
public class ProgramOrderableImportPersisterTest {

  private static final String PRICE_PER_PACK = "123";

  private InputStream dataStream;
  private ProgramOrderableCsvModel csvModel;
  private ProgramOrderable programOrderable;
  private OrderableDisplayCategory orderableDisplayCategory;

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Mock
  private FileHelper fileHelper;

  @Mock
  private ProgramOrderableRepository programOrderableRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @InjectMocks
  private ProgramOrderableImportPersister programOrderableImportPersister;

  @Before
  public void setUp() {
    // Set environment variables
    environmentVariables.set("CURRENCY_CODE", "USD");

    // Initialize mock objects
    dataStream = mock(InputStream.class);
    csvModel = mock(ProgramOrderableCsvModel.class);
    programOrderable = mock(ProgramOrderable.class);
    orderableDisplayCategory = mock(OrderableDisplayCategory.class);

    // Set up mock behaviors
    when(csvModel.getPricePerPack()).thenReturn(PRICE_PER_PACK);
    when(orderableDisplayCategory.getOrderedDisplayValue()).thenReturn(
        mock(OrderedDisplayValue.class));
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistData() {
    // Given
    setupMocksForSuccess();

    // When
    List<ProgramOrderableDto> result = programOrderableImportPersister
        .processAndPersist(dataStream);

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(ProgramOrderableCsvModel.class, dataStream);
    verify(programOrderableRepository).saveAll(any());
  }

  private void setupMocksForSuccess() {
    when(fileHelper.readCsv(ProgramOrderableCsvModel.class, dataStream))
        .thenReturn(Collections.singletonList(csvModel));
    when(programRepository.findByCode(any(Code.class))).thenReturn(mock(Program.class));
    when(orderableRepository.findByProductCode(any(Code.class))).thenReturn(mock(Orderable.class));
    when(orderableDisplayCategoryRepository.findByCode(any(Code.class))).thenReturn(
        orderableDisplayCategory);
    when(programOrderableRepository.saveAll(any()))
        .thenReturn(Collections.singletonList(programOrderable));
  }

}
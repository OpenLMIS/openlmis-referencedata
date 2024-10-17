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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.ProgramOrderableCsvModel;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.slf4j.profiler.Profiler;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProgramOrderableImportPersisterTest {

  private InputStream dataStream;

  @Mock private FileHelper fileHelper;
  @Mock private ProgramOrderableRepository programOrderableRepository;
  @Mock private ProgramRepository programRepository;
  @Mock private OrderableRepository orderableRepository;
  @Mock private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;
  @Mock private TransactionUtils transactionUtils;

  @InjectMocks private ProgramOrderableImportPersister programOrderableImportPersister;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(programOrderableImportPersister, "currencyCode", "USD");

    ReflectionTestUtils.setField(
        programOrderableImportPersister,
        "importExecutorService",
        MoreExecutors.newDirectExecutorService());

    final Program program = new ProgramDataBuilder().build();
    final Orderable orderable = new OrderableDataBuilder().build();
    final OrderableDisplayCategory orderableDisplayCategory =
        new OrderableDisplayCategoryDataBuilder().build();
    final ProgramOrderableCsvModel csvModel =
        new ProgramOrderableCsvModel(
            program.getCode().toString(),
            orderable.getProductCode().toString(),
            2,
            true,
            orderableDisplayCategory.getCode().toString(),
            true,
            1,
            "123");

    dataStream = mock(InputStream.class);

    when(transactionUtils.runInOwnTransaction(any(Supplier.class)))
        .thenAnswer(invocation -> ((Supplier) invocation.getArgument(0)).get());
    when(fileHelper.readCsv(ProgramOrderableCsvModel.class, dataStream))
        .thenReturn(singletonList(csvModel));
    when(programOrderableRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(programRepository.findAllByCodeIn(any())).thenReturn(singletonList(program));
    when(orderableRepository.findAllLatestByProductCode(any()))
        .thenReturn(singletonList(orderable));
    when(orderableDisplayCategoryRepository.findAllByCodeIn(any()))
        .thenReturn(singletonList(orderableDisplayCategory));
    when(programOrderableRepository
            .findAllByProgramCodeInAndProductCodeInAndOrderableDisplayCategoryCodeIn(
                any(), any(), any()))
        .thenReturn(emptyList());
  }

  @Test
  public void shouldSuccessfullyProcessAndPersistData() throws InterruptedException {
    // When
    List<ProgramOrderableDto> result =
        programOrderableImportPersister.processAndPersist(dataStream, mock(Profiler.class));

    // Then
    assertEquals(1, result.size());
    verify(fileHelper).readCsv(ProgramOrderableCsvModel.class, dataStream);
    verify(programOrderableRepository).saveAll(any());
  }
}

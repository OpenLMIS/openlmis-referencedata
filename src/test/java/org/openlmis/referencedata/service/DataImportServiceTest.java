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

package org.openlmis.referencedata.service;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.DataImportService.ORDERABLE_CSV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.OrderableBuilder;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DataImportServiceTest {

  private static final List<String> ORDERABLE_CORRECT_HEADERS = Arrays.asList(
      "productCode", "name", "description", "packRoundingThreshold",
      "packSize", "roundToZero", "dispensable");

  private static final List<List<String>> ORDERABLE_CORRECT_RECORDS = Arrays.asList(
      Arrays.asList(
          "0002-1975", "Levonorgestrel", "Product description goes here.", "0",
          "1", "false", "dispensingUnit:10 tab strip"),
      Arrays.asList(
          "0002-8400", "Glucagon", "Product description goes here.", "3",
          "94", "false", "sizeCode:2 dose")
  );

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private OrderableBuilder orderableBuilder;

  @InjectMocks
  private DataImportService dataImportService;

  @Test
  public void shouldImportOrderableData() throws IOException {
    List<Orderable> importedObjects = Arrays.asList(mock(Orderable.class), mock(Orderable.class));

    when(orderableRepository.findFirstByProductCodeOrderByIdentityVersionNumberDesc(
            any(Code.class))).thenReturn(mock(Orderable.class));

    when(orderableBuilder.newOrderable(any(OrderableDto.class),
        any(Orderable.class))).thenReturn(mock(Orderable.class));

    when(orderableRepository.saveAll(any(List.class))).thenReturn(importedObjects);

    MockMultipartFile zip = createCsvAndZipIt(
        ORDERABLE_CORRECT_RECORDS, ORDERABLE_CORRECT_HEADERS, ORDERABLE_CSV);

    List<?> result = dataImportService.importData(zip);
    assertFalse(result.isEmpty());
  }

  private MockMultipartFile createCsvAndZipIt(List<List<String>> fields,
                                              List<String> headers,
                                              String fileName) throws IOException {
    ByteArrayOutputStream csvOutputStream = new ByteArrayOutputStream();
    OutputStreamWriter csvWriter = new OutputStreamWriter(csvOutputStream);

    CSVPrinter csvPrinter = new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
    csvPrinter.printRecord(headers);
    for (List<String> row: fields) {
      csvPrinter.printRecord(row);
    }
    csvPrinter.close();

    ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(zipOutputStream);

    ZipEntry zipEntry = new ZipEntry(fileName);
    zip.putNextEntry(zipEntry);
    zip.write(csvOutputStream.toByteArray());
    zip.closeEntry();

    return new MockMultipartFile(fileName, fileName,
        "application/zip", zipOutputStream.toByteArray());
  }

}
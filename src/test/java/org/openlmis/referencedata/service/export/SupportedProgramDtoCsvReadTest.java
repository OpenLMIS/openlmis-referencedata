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
import static org.mockito.ArgumentMatchers.anyBoolean;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.SupportedProgramCsvModel;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class SupportedProgramDtoCsvReadTest {
  @Mock private CsvHeaderValidator csvHeaderValidator;
  @InjectMocks private FileHelper fileHelper;

  @Before
  public void setup() {
    Mockito.doNothing()
        .when(csvHeaderValidator)
        .validateHeaders(any(List.class), any(ModelClass.class), anyBoolean());
  }

  @Test
  public void shouldReadSupportedProgramDtoFromCsv() throws IOException {
    final SupportedProgramCsvModel expectedModel1 = new SupportedProgramCsvModel();
    expectedModel1.setProgramCode("TestProgram1");
    expectedModel1.setFacilityCode("TestFacility1");
    expectedModel1.setActive(true);
    expectedModel1.setLocallyFulfilled(true);
    expectedModel1.setStartDate(LocalDate.of(2024, 10, 8));

    final InputStream csvStream =
        new ClassPathResource(
                "/SupportedProgramDtoCsvReadTest/shouldReadSupportedProgramDtoFromCsv.csv")
            .getInputStream();
    final List<SupportedProgramCsvModel> supportedProgramCsvModels =
        fileHelper.readCsv(SupportedProgramCsvModel.class, csvStream);

    assertEquals(1, supportedProgramCsvModels.size());
    assertEquals(expectedModel1, supportedProgramCsvModels.get(0));
  }
}

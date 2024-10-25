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

package org.openlmis.referencedata.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.openlmis.referencedata.domain.RightName.DATA_IMPORT;
import static org.openlmis.referencedata.web.export.DataImportController.RESOURCE_PATH;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.slf4j.profiler.Profiler;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class DataImportControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final MultipartFile file = new MockMultipartFile(
      "orderable.csv", "test-data".getBytes());
  private final Orderable orderable = new OrderableDataBuilder().build();
  private final OrderableDto orderableDto = OrderableDto.newInstance(orderable);

  @Before
  @Override
  public void setUp() {
    super.setUp();
    mockUserHasRight(DATA_IMPORT);

    try {
      given(dataImportService.importData(any(MultipartFile.class), any(Profiler.class)))
          .willReturn(Collections.singletonList(orderableDto));
    } catch (InterruptedException ie) {
      // ignore in tests
    }
  }

  @Test
  public void shouldImportDataWithCorrectRight() throws IOException {
    restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .multiPart("file", file.getOriginalFilename(), file.getInputStream())
            .when()
            .post(RESOURCE_PATH)
            .then()
            .statusCode(200)
            .extract().response();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotImportDataWithIncorrectRight() throws IOException {
    mockUserHasNoRight(DATA_IMPORT);

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .multiPart("file", file.getOriginalFilename(), file.getInputStream())
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}

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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.openlmis.referencedata.domain.RightName.DATA_EXPORT;
import static org.openlmis.referencedata.web.DataExportController.RESOURCE_PATH;
import static org.openlmis.referencedata.web.DataExportController.ZIP_MEDIA_TYPE;
import static org.openlmis.referencedata.web.DataExportParams.DATA;
import static org.openlmis.referencedata.web.DataExportParams.FORMAT;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.util.messagekeys.DataExportMessageKeys;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;

public class DataExportControllerIntegrationTest extends BaseWebIntegrationTest {

  private final Map<String, String> queryParamsMap = new HashMap<>();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    queryParamsMap.put(FORMAT, "csv");
    queryParamsMap.put(DATA, "orderable");

    mockUserHasRight(DATA_EXPORT);
  }

  @Test
  public void shouldReturnZipArchiveBytes() throws IOException {
    ClassPathResource file = new ClassPathResource("csv/export_results.zip");
    byte[] zipBytes = FileUtils.readFileToByteArray(file.getFile());
    given(dataExportService.exportData(any(DataExportParams.class))).willReturn(zipBytes);

    Response response = restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(ZIP_MEDIA_TYPE)
            .queryParameters(queryParamsMap)
            .when()
            .get(RESOURCE_PATH)
            .then()
            .statusCode(200)
            .extract().response();

    verify(dataExportService).exportData(any(DataExportParams.class));
    assertEquals(response.getContentType(), ZIP_MEDIA_TYPE);
    assertArrayEquals(response.getBody().asByteArray(), zipBytes);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDataExportRequestIfUserHasNoRight() {
    mockUserHasNoRight(DATA_EXPORT);

    String response = restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(ZIP_MEDIA_TYPE)
            .queryParameters(queryParamsMap)
            .when()
            .get(RESOURCE_PATH)
            .then()
            .statusCode(403)
            .extract()
            .path(MESSAGE_KEY);

    assertThat(response, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenFormatParameterIsMissing() {
    queryParamsMap.remove(FORMAT);

    String response = getPathAsString();

    assertEquals(response, DataExportMessageKeys.ERROR_MISSING_FORMAT_PARAMETER);
  }

  @Test
  public void shouldReturnBadRequestWhenDataParameterIsMissing() {
    queryParamsMap.remove(DATA);

    String response = getPathAsString();

    assertEquals(response, DataExportMessageKeys.ERROR_MISSING_DATA_PARAMETER);
  }

  @Test
  public void shouldReturnBadRequestWhenNoParametersProvided() {
    queryParamsMap.clear();

    String response = getPathAsString();

    assertEquals(response, DataExportMessageKeys.ERROR_LACK_PARAMS);
  }

  private String getPathAsString() {
    return restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(ZIP_MEDIA_TYPE)
            .queryParameters(queryParamsMap)
            .when()
            .get(RESOURCE_PATH)
            .then()
            .statusCode(400)
            .extract()
            .path(MESSAGE_KEY);
  }

}
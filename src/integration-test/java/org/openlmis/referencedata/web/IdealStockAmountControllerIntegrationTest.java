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

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class IdealStockAmountControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/idealStockAmounts";

  @MockBean
  private IdealStockAmountRepository repository;

  private IdealStockAmount isa;

  @Before
  public void setUp() {
    final Facility facility = new Facility("facility-code");
    final Program program = new Program("program-code");
    final Orderable orderable = new Orderable(Code.code("product-code"),
        Dispensable.createNew("each"), "fullname", "description", 10, 1, true, new HashSet<>(),
        new HashMap<>(), new HashMap<>());
    ProcessingPeriod period = new ProcessingPeriod();
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode("schedule-code");
    period.setProcessingSchedule(schedule);
    period.setName("period");
    period.setStartDate(LocalDate.of(2017, 8, 25));
    period.setEndDate(LocalDate.of(2017, 9, 25));

    isa = new IdealStockAmount(facility, program, orderable, period, 1200);
  }

  @Test
  public void shouldRetrieveAllIdealStockAmounts() {
    when(repository.findAll(any(Pageable.class)))
        .thenReturn(Pagination.getPage(Arrays.asList(isa), null, 1));

    PageImplRepresentation response = getCatalogItems(null, null)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(response.getContent().size(), 1);
    verifyNoMoreInteractions(rightService);
    verify(repository).findAll(any(Pageable.class));
    //assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownloadCsvWithAllPossibleFields() throws IOException {
    mockUserHasRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNT_MANAGE);
    when(repository.findAll()).thenReturn(Arrays.asList(isa));

    String csvContent = download()
        .then()
        .statusCode(200)
        .extract().body().asString();

    verify(repository).findAll();
    assertEquals("Facility Code,Program Code,Product Code,Period,Ideal Stock Amount\r\n"
        + "facility-code,program-code,product-code,schedule-code period,1200\r\n", csvContent);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownloadCsvWithHeadersOnly() throws IOException {
    mockUserHasRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNT_MANAGE);
    when(repository.findAll())
        .thenReturn(Collections.emptyList());

    String csvContent = download()
        .then()
        .statusCode(200)
        .extract().body().asString();

    verify(repository).findAll();
    assertEquals("Facility Code,Program Code,Product Code,Period,Ideal Stock Amount\r\n",
        csvContent);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenUploadCsvIfUserHasNoIdealStockAmountManage()
      throws IOException {
    mockUserHasNoRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNT_MANAGE);

    String messageKey = download()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    // changed to responseChecks because file parameter is required
    // and RAML check does not recognizes it in request
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  private Response download() {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType("text/csv")
        .queryParam("format", "csv")
        .when()
        .get(RESOURCE_URL);
  }

  private Response getCatalogItems(Integer page, Integer size) {
    RequestSpecification request = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    if (page != null) {
      request = request.param("page", page);
    }
    if (size != null) {
      request = request.param("size", size);
    }

    return request.when().get(RESOURCE_URL);
  }
}

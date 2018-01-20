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

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_MISSING_MANDATORY_COLUMNS;
import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_RECORD_INVALID;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.UploadResultDto;
import org.openlmis.referencedata.service.IdealStockAmountSearchParams;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class IdealStockAmountControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/idealStockAmounts";
  private static final String FILE_PARAM_NAME = "file";

  private IdealStockAmount isa;
  private Facility facility;
  private CommodityType commodityType;
  private ProcessingPeriod period;
  private ProcessingSchedule schedule;

  @Before
  public void setUp() {
    facility = new Facility("facility-code");
    GeographicLevel countryLevel = new GeographicLevel("Country", 1);
    facility.setGeographicZone(new GeographicZone("TC", countryLevel));

    commodityType = new CommodityType("Name", "cSys", "cId", null, new ArrayList<>());

    schedule = new ProcessingSchedule();
    schedule.setCode("schedule-code");
    schedule.setDescription("desc");
    schedule.setId(UUID.randomUUID());
    schedule.setModifiedDate(ZonedDateTime.now());
    schedule.setName("schedule");

    period = new ProcessingPeriod();
    period.setProcessingSchedule(schedule);
    period.setName("period");
    period.setStartDate(LocalDate.of(2017, 8, 25));
    period.setEndDate(LocalDate.of(2017, 9, 25));

    isa = new IdealStockAmount(facility, commodityType, period, 1200);

    when(idealStockAmountRepository.save(any(IdealStockAmount.class)))
        .thenAnswer(new SaveAnswer<IdealStockAmount>());

    when(facilityRepository.findByCode(any(String.class))).thenReturn(Optional.of(facility));
    when(periodRepository.findOneByNameAndProcessingSchedule(any(String.class),
        any(ProcessingSchedule.class)))
        .thenReturn(Optional.of(period));
    when(scheduleRepository.findOneByCode(any(String.class))).thenReturn(Optional.of(schedule));
    when(commodityTypeRepository.findByClassificationIdAndClassificationSystem(any(String.class),
        any(String.class))).thenReturn(Optional.of(commodityType));

    mockUserHasRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE);
  }

  @Test
  public void shouldRetrieveIdealStockAmountsBasedOnParams() {
    UUID facilityId = UUID.randomUUID();
    UUID commodityTypeId = UUID.randomUUID();
    UUID processingPeriodId = UUID.randomUUID();

    when(idealStockAmountService
        .search(any(IdealStockAmountSearchParams.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(isa)));

    PageImplRepresentation response =
        getIdealStockAmounts(facilityId, commodityTypeId, processingPeriodId, 2, 10)
            .then()
            .statusCode(200)
            .extract().as(PageImplRepresentation.class);

    assertEquals(10, response.getSize());
    assertEquals(2, response.getNumber());
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(21, response.getTotalElements());
    assertEquals(3, response.getTotalPages());

    IdealStockAmountSearchParams searchParams =
        new IdealStockAmountSearchParams(facilityId, commodityTypeId, processingPeriodId);
    verify(idealStockAmountService).search(refEq(searchParams),any(Pageable.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownloadCsvWithAllPossibleFields() throws IOException {

    when(idealStockAmountService.search()).thenReturn(singletonList(isa));

    String csvContent = download()
        .then()
        .statusCode(200)
        .extract().body().asString();

    verify(idealStockAmountService).search();
    assertEquals("Facility Code,Commodity Type,Period,Ideal Stock Amount\r\n"
        + joinWith(",", facility.getCode(),
        StringUtils.joinWith("|", commodityType.getClassificationSystem(),
            commodityType.getClassificationId()),
        StringUtils.joinWith("|", schedule.getCode(), period.getName()),
        isa.getAmount()) + "\r\n", csvContent);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownloadCsvWithHeadersOnly() throws IOException {

    when(idealStockAmountService.search())
        .thenReturn(Collections.emptyList());

    String csvContent = download()
        .then()
        .statusCode(200)
        .extract().body().asString();

    verify(idealStockAmountService).search();
    assertEquals("Facility Code,Commodity Type,Period,Ideal Stock Amount\r\n",
        csvContent);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void downloadShouldReturnUnauthorizedWithoutAuthorization() throws IOException {

    restAssured.given()
        .contentType("text/csv")
        .queryParam("format", "csv")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    // changed to responseChecks because file parameter is required
    // and RAML check does not recognizes it in request
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldUploadCsvWithAllFields() throws IOException {
    ClassPathResource fullCsvToUpload =
        new ClassPathResource("csv/idealStockAmounts/csvWithAllColumns.csv");

    UploadResultDto result = upload(fullCsvToUpload)
        .then()
        .statusCode(200)
        .extract().as(UploadResultDto.class);

    verify(idealStockAmountRepository).save(anyListOf(IdealStockAmount.class));
    assertEquals(1, result.getAmount().intValue());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldNotUploadCsvWithoutMandatoryFields() throws IOException {
    ClassPathResource basicCsvToUpload =
        new ClassPathResource("csv/idealStockAmounts/csvWithMissingMandatoryColumns.csv");

    String messageKey = upload(basicCsvToUpload)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_UPLOAD_MISSING_MANDATORY_COLUMNS)));
    verify(idealStockAmountRepository, never()).save(any(IdealStockAmount.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldNotUploadCsvWithInvalidColumnValues() throws IOException {
    ClassPathResource basicCsvToUpload =
        new ClassPathResource("csv/idealStockAmounts/csvWithInvalidColumnValues.csv");

    String messageKey = upload(basicCsvToUpload)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_UPLOAD_RECORD_INVALID)));
    verify(idealStockAmountRepository, never()).save(any(IdealStockAmount.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnUnauthorizedWhenUploadCsvIfUserHasNoCceManagePermission()
      throws IOException {
    mockUserHasNoRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE);
    ClassPathResource basicCsvToUpload =
        new ClassPathResource("csv/idealStockAmounts/csvWithAllColumns.csv");

    String messageKey = upload(basicCsvToUpload)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
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

  private Response upload(ClassPathResource basicCsvToUpload) throws IOException {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        .queryParam("format", "csv")
        .multiPart(FILE_PARAM_NAME,
            basicCsvToUpload.getFilename(),
            basicCsvToUpload.getInputStream())
        .when()
        .post(RESOURCE_URL);
  }

  private Response getIdealStockAmounts(UUID facilityId, UUID commodityTypeId,
                                        UUID processingPeriodId, Integer page, Integer size) {
    RequestSpecification request = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .param("facilityId", facilityId)
        .param("commodityTypeId", commodityTypeId)
        .param("processingPeriodId", processingPeriodId);

    if (page != null) {
      request = request.param("page", page);
    }
    if (size != null) {
      request = request.param("size", size);
    }

    return request.when().get(RESOURCE_URL);
  }
}

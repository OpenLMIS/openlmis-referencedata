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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.domain.RightName.FACILITY_APPROVED_ORDERABLES_MANAGE;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ApprovedProductDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductSearchParamsDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDisplayCategoryDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityTypeApprovedProductControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/facilityTypeApprovedProducts";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private static final String FACILITY_TYPE_PARAM = "facilityType";
  private static final String PROGRAM_PARAM = "program";
  private static final String ORDERABLE_PARAM = "orderableId";

  private Program program;
  private Orderable orderable;
  private FacilityType facilityType1;
  private FacilityTypeApprovedProduct facilityTypeAppProd;
  private ApprovedProductDto ftapDto;
  private UUID facilityTypeAppProdId;
  private OrderableDisplayCategory orderableDisplayCategory;

  @Before
  @Override
  public void setUp() {
    program = new ProgramDataBuilder().build();
    orderableDisplayCategory = new OrderableDisplayCategoryDataBuilder().build();
    orderable = new OrderableDataBuilder().build();
    facilityType1 = new FacilityTypeDataBuilder().build();

    facilityTypeAppProd = new FacilityTypeApprovedProductsDataBuilder()
        .withFacilityType(facilityType1)
        .withProgram(program)
        .withOrderableId(orderable.getId())
        .withMaxPeriodsOfStock(6.0)
        .withActive(true)
        .build();

    facilityTypeAppProdId = facilityTypeAppProd.getId();


    ftapDto = new ApprovedProductDto();
    facilityTypeAppProd.export(ftapDto);
    ftapDto.setOrderable(orderable);

    given(facilityTypeApprovedProductRepository.save(any(FacilityTypeApprovedProduct.class)))
        .willAnswer(invocation -> {
          FacilityTypeApprovedProduct resource = invocation
              .getArgument(0, FacilityTypeApprovedProduct.class);
          resource.updateLastUpdatedDate();

          return resource;
        });

    // used in deserialization
    given(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionNumberDesc(
        orderable.getId())).willReturn(orderable);
    given(orderableRepository
        .findAllLatestByIds(Collections.singleton(orderable.getId()), PageRequest.of(0, 1)))
        .willReturn(new PageImpl<>(Collections.singletonList(orderable)));
    given(programRepository.findById(program.getId())).willReturn(Optional.of(program));
    given(facilityTypeRepository.findById(facilityType1.getId()))
        .willReturn(Optional.of(facilityType1));

    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);
  }

  // DELETE /facilityTypeApprovedProducts/{id}

  @Test
  public void shouldDeleteFacilityTypeApprovedProduct() {
    given(facilityTypeApprovedProductRepository
        .findByIdentityId(facilityTypeAppProdId))
        .willReturn(Collections.singletonList(facilityTypeAppProd));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInDeleteFacilityTypeApprovedProduct() {
    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightToDeleteFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundForNonExistingResourceVersionInDeleteEndpoint() {
    Long versionNumber = 1000L;

    given(facilityTypeApprovedProductRepository
        .findByIdentityIdAndIdentityVersionNumber(facilityTypeAppProdId, versionNumber))
        .willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .queryParam("versionNumber", versionNumber)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /facilityTypeApprovedProducts

  @Test
  public void shouldPostFacilityTypeApprovedProduct() {
    given(programRepository.findById(program.getId())).willReturn(Optional.of(program));
    given(orderableDisplayCategoryRepository.findById(orderableDisplayCategory.getId()))
        .willReturn(Optional.of(orderableDisplayCategory));

    ftapDto.setId(null);

    ApprovedProductDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .extract().as(ApprovedProductDto.class);

    ftapEquals(ftapDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfIdFieldProvidedFtapPost() {
    ftapDto.setId(UUID.randomUUID());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(LocalizedMessage.MESSAGE_KEY_FIELD,
            is(FacilityTypeApprovedProductMessageKeys.ERROR_ID_PROVIDED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPostFacilityTypeApprovedProduct() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNoRightsForPostFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // PUT /facilityTypeApprovedProducts/{id}

  @Test
  public void shouldUpdateFacilityTypeApprovedProduct() {
    FacilityTypeApprovedProduct original = new FacilityTypeApprovedProductsDataBuilder()
        .build();

    facilityTypeAppProd.setMaxPeriodsOfStock(9.00);
    facilityTypeAppProd.export(ftapDto);

    Long versionNumber = original.getVersionNumber() + 1;

    given(facilityTypeApprovedProductRepository
        .findFirstByIdentityIdOrderByIdentityVersionNumberDesc(facilityTypeAppProdId))
        .willReturn(original);

    ApprovedProductDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .body(ftapDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .body("meta.versionNumber", is(versionNumber.intValue()))
        .extract()
        .as(ApprovedProductDto.class);

    ftapEquals(ftapDto, response);
    assertEquals(9.00, response.getMaxPeriodsOfStock(), 0.00);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfIdMismatchForPutFacilityTypeApprovedProduct() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", UUID.randomUUID())
        .body(ftapDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(FacilityTypeApprovedProductMessageKeys.ERROR_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPutFacilityTypeApprovedProduct() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .body(ftapDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNoRightsToPutFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .body(ftapDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /facilityTypeApprovedProducts/{id}

  @Test
  public void shouldGetFacilityTypeApprovedProduct() {
    given(facilityTypeApprovedProductRepository
        .findFirstByIdentityIdOrderByIdentityVersionNumberDesc(facilityTypeAppProdId))
        .willReturn(facilityTypeAppProd);

    ApprovedProductDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .as(ApprovedProductDto.class);

    ftapEquals(ftapDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInGetFacilityTypeApprovedProduct() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundForNonExistingResourceVersionInGetFacilityTypeApprovedProduct() {
    Long versionNumber = 1000L;

    given(facilityTypeApprovedProductRepository
        .findByIdentityIdAndIdentityVersionNumber(facilityTypeAppProdId, versionNumber))
        .willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .queryParam("versionNumber", versionNumber)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /facilityTypeApprovedProducts

  @Test
  public void shouldSearchFtaps() {
    given(facilityTypeApprovedProductRepository
        .searchProducts(any(QueryFacilityTypeApprovedProductSearchParams.class),
            any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(facilityTypeAppProd),
            PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(FACILITY_TYPE_PARAM, facilityType1.getCode())
        .queryParam(PROGRAM_PARAM, program.getCode().toString())
        .queryParam(ORDERABLE_PARAM, orderable.getId())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(ftapDto.getId().toString(),
        ((java.util.LinkedHashMap) response.getContent().get(0)).get("id"));
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInSearchEndpoint() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /facilityTypeApprovedProducts

  @Test
  public void shouldPostSearchFtaps() {
    FacilityTypeApprovedProductSearchParams searchParams =
        new FacilityTypeApprovedProductSearchParamsDataBuilder()
        .withFacilityTypeCode(facilityType1.getCode())
        .withProgramCode(program.getCode().toString())
        .withIdentity(facilityTypeAppProd.getId(), facilityTypeAppProd.getVersionNumber())
        .build();

    given(facilityTypeApprovedProductRepository
        .searchProducts(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(facilityTypeAppProd),
            PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(ftapDto.getId().toString(),
        ((java.util.LinkedHashMap) response.getContent().get(0)).get("id"));
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPostSearchEndpoint() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(new FacilityTypeApprovedProductSearchParams())
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  // GET /facilityTypeApprovedProducts/{id}/auditLog

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository
        .findFirstByIdentityIdOrderByIdentityVersionNumberDesc(any(UUID.class)))
        .willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository
        .findFirstByIdentityIdOrderByIdentityVersionNumberDesc(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository
        .findFirstByIdentityIdOrderByIdentityVersionNumberDesc(any(UUID.class)))
        .willReturn(facilityTypeAppProd);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void ftapEquals(ApprovedProductDto expected, ApprovedProductDto response) {
    if (expected.getId() != null) {
      assertEquals(expected.getId(), response.getId());
    }
    assertEquals(expected.getEmergencyOrderPoint(), response.getEmergencyOrderPoint());
    assertEquals(expected.getMaxPeriodsOfStock(), response.getMaxPeriodsOfStock());
    assertEquals(expected.getMinPeriodsOfStock(), response.getMinPeriodsOfStock());
    assertEquals(expected.getFacilityType().getId(), response.getFacilityType().getId());
    assertEquals(expected.getProgram().getId(), response.getProgram().getId());
    assertEquals(expected.getOrderable().getId(), response.getOrderable().getId());
    assertEquals(expected.getOrderable().getVersionNumber(),
        response.getOrderable().getVersionNumber());
  }

}

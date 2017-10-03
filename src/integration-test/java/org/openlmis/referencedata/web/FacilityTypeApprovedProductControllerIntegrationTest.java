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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.FACILITY_APPROVED_ORDERABLES_MANAGE;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ApprovedProductDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityTypeApprovedProductControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/facilityTypeApprovedProducts";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private Program program;
  private Orderable orderable;
  private FacilityType facilityType1;
  private FacilityTypeApprovedProduct facilityTypeAppProd;
  private ApprovedProductDto ftapDto;
  private UUID facilityTypeAppProdId;
  private OrderableDisplayCategory orderableDisplayCategory;

  @Before
  public void setUp() {

    program = new Program("programCode");
    program.setPeriodsSkippable(true);
    program.setId(UUID.randomUUID());

    orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode"),
        new OrderedDisplayValue("orderableDisplayCategoryName", 1));
    orderableDisplayCategory.setId(UUID.randomUUID());

    HashMap<String, String> identificators = new HashMap<>();
    HashMap<String, String> extraData = new HashMap<>();
    orderable = new Orderable(Code.code("abcd"), Dispensable.createNew("each"),
        "Abcd", "description", 10, 5, false, Collections.emptySet(), identificators,
        extraData);
    orderable.setId(UUID.randomUUID());

    facilityType1 = new FacilityType("facilityType1");
    facilityType1.setId(UUID.randomUUID());

    facilityTypeAppProdId = UUID.randomUUID();
    facilityTypeAppProd = new FacilityTypeApprovedProduct();
    facilityTypeAppProd.setId(facilityTypeAppProdId);
    facilityTypeAppProd.setFacilityType(facilityType1);
    facilityTypeAppProd.setProgram(program);
    facilityTypeAppProd.setOrderable(orderable);
    facilityTypeAppProd.setMaxPeriodsOfStock(6.00);

    ftapDto = new ApprovedProductDto();
    facilityTypeAppProd.export(ftapDto);

    given(facilityTypeApprovedProductRepository.save(any(FacilityTypeApprovedProduct.class)))
        .willAnswer(new SaveAnswer<FacilityTypeApprovedProduct>());

    // used in deserialization
    given(orderableRepository.findOne(orderable.getId())).willReturn(orderable);
  }

  @Test
  public void shouldDeleteFacilityTypeApprovedProduct() {

    given(facilityTypeApprovedProductRepository.findOne(facilityTypeAppProdId))
        .willReturn(facilityTypeAppProd);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403IfUserHasNoRightToDeleteFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostFacilityTypeApprovedProduct() {
    given(programRepository.findOne(program.getId())).willReturn(program);
    given(orderableDisplayCategoryRepository.findOne(orderableDisplayCategory.getId()))
        .willReturn(orderableDisplayCategory);

    ftapDto.setId(null);

    ApprovedProductDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ApprovedProductDto.class);

    ftapEquals(ftapDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToPostFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldPutFacilityTypeApprovedProduct() {

    facilityTypeAppProd.setMaxPeriodsOfStock(9.00);
    ftapDto.setId(UUID.randomUUID());
    facilityTypeAppProd.export(ftapDto);
    given(facilityTypeApprovedProductRepository.findOne(facilityTypeAppProdId))
        .willReturn(facilityTypeAppProd);
    given(programRepository.findOne(program.getId())).willReturn(program);
    given(orderableDisplayCategoryRepository.findOne(orderableDisplayCategory.getId()))
        .willReturn(orderableDisplayCategory);

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
        .extract().as(ApprovedProductDto.class);

    ftapEquals(ftapDto, response);
    assertEquals(9.00, response.getMaxPeriodsOfStock(), 0.00);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToPutFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .body(ftapDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetFacilityTypeApprovedProduct() {

    given(facilityTypeApprovedProductRepository.findOne(facilityTypeAppProdId))
        .willReturn(facilityTypeAppProd);
    given(programRepository.findOne(program.getId())).willReturn(program);
    given(orderableDisplayCategoryRepository.findOne(orderableDisplayCategory.getId()))
        .willReturn(orderableDisplayCategory);

    ApprovedProductDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ApprovedProductDto.class);

    assertEquals(ftapDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToGetFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestForDuplicateFtapPut() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    FacilityTypeApprovedProduct existingFtap =
        mock(FacilityTypeApprovedProduct.class);
    when(existingFtap.getId()).thenReturn(UUID.randomUUID());
    when(facilityTypeApprovedProductRepository.findByFacilityTypeIdAndOrderableIdAndProgramId(
      facilityType1.getId(), orderable.getId(), program.getId()
    )).thenReturn(existingFtap);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", ftapDto.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .put(ID_URL)
        .then()
        .statusCode(400)
        .content(LocalizedMessage.MESSAGE_KEY_FIELD,
            equalTo(FacilityTypeApprovedProductMessageKeys.ERROR_DUPLICATED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
    verify(facilityTypeApprovedProductRepository,
        never()).save(any(FacilityTypeApprovedProduct.class));
  }

  @Test
  public void shouldReturnBadRequestForDuplicateFtapPost() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    FacilityTypeApprovedProduct existingFtap =
        mock(FacilityTypeApprovedProduct.class);
    when(existingFtap.getId()).thenReturn(UUID.randomUUID());
    when(facilityTypeApprovedProductRepository.findByFacilityTypeIdAndOrderableIdAndProgramId(
        facilityType1.getId(), orderable.getId(), program.getId()
    )).thenReturn(existingFtap);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .content(LocalizedMessage.MESSAGE_KEY_FIELD,
            equalTo(FacilityTypeApprovedProductMessageKeys.ERROR_DUPLICATED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
    verify(facilityTypeApprovedProductRepository,
        never()).save(any(FacilityTypeApprovedProduct.class));
  }

  @Test
  public void shouldUpdateExistingFtap() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    FacilityTypeApprovedProduct existingFtap =
        mock(FacilityTypeApprovedProduct.class);
    when(existingFtap.getId()).thenReturn(facilityTypeAppProdId);
    when(facilityTypeApprovedProductRepository.findByFacilityTypeIdAndOrderableIdAndProgramId(
        facilityType1.getId(), orderable.getId(), program.getId()
    )).thenReturn(existingFtap);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", ftapDto.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ftapDto)
        .log().body()
        .put(ID_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
    verify(facilityTypeApprovedProductRepository).save(facilityTypeAppProd);
  }

  @Test
  public void shouldSearchFtaps() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    given(facilityTypeApprovedProductRepository
        .searchProducts(eq(facilityType1.getCode()), eq(program.getCode().toString()),
            any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(facilityTypeAppProd)));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityType", facilityType1.getCode())
        .queryParam("program", program.getCode().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(ftapDto.getId().toString(),
        ((java.util.LinkedHashMap)response.getContent().get(0)).get("id"));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPaginateSearchFtaps() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    Pageable pageable = new PageRequest(0, 10);
    given(facilityTypeApprovedProductRepository
        .searchProducts(facilityType1.getCode(), program.getCode().toString(), pageable))
        .willReturn(Pagination.getPage(Lists.newArrayList(facilityTypeAppProd)));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .queryParam("facilityType", facilityType1.getCode())
        .queryParam("program", program.getCode().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(10, response.getSize());
    assertEquals(0, response.getNumber());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToSearchFacilityTypeApprovedProduct() {
    mockUserHasNoRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn400WhenInvalidSearchParamsAreProvided() {
    mockUserHasRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITY_APPROVED_ORDERABLES_MANAGE);
    given(facilityTypeApprovedProductRepository.findOne(any(UUID.class)))
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
    assertEquals(expected.getFacilityType(), response.getFacilityType());
    assertEquals(expected.getOrderable(), response.getOrderable());
    assertEquals(expected.getProgram(), response.getProgram());
  }

}

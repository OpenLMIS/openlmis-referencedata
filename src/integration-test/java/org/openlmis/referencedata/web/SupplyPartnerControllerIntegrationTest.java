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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyPartner;
import org.openlmis.referencedata.dto.SupplyPartnerDto;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerAssociationDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyPartnerDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.SupplyPartnerMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyPartnerControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_PATH = SupplyPartnerController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_PATH + SupplyPartnerController.ID_URL;

  private Program program = new ProgramDataBuilder().build();
  private SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder().build();
  private Facility facility = new FacilityDataBuilder().build();
  private Orderable orderable = new OrderableDataBuilder().build();
  private SupplyPartner supplyPartner = new SupplyPartnerDataBuilder()
      .withAssociation(
          new SupplyPartnerAssociationDataBuilder()
              .withProgram(program)
              .withSupervisoryNode(supervisoryNode)
              .withFacility(facility)
              .withOrderable(orderable)
              .build())
      .build();

  private SupplyPartnerDto supplyPartnerDto = new SupplyPartnerDto();

  @Before
  @Override
  public void setUp() {
    super.setUp();
    supplyPartnerDto.setServiceUrl(baseUri);
    supplyPartner.export(supplyPartnerDto);

    given(supplyPartnerRepository.save(any(SupplyPartner.class)))
        .willAnswer(new SaveAnswer<>());

    given(programRepository.findOne(program.getId())).willReturn(program);
    given(supervisoryNodeRepository.findOne(supervisoryNode.getId())).willReturn(supervisoryNode);
    given(facilityRepository.findAll(Sets.newHashSet(facility.getId())))
        .willReturn(Lists.newArrayList(facility));
    given(orderableRepository.findAllLatestByIds(
        Sets.newHashSet(orderable.getId()), new PageRequest(0, 1)))
        .willReturn(Pagination.getPage(Lists.newArrayList(orderable)));

    mockUserHasRight(RightName.SUPPLY_PARTNER_MANAGE);
  }

  @Test
  public void shouldGetSupplyPartners() {
    given(supplyPartnerRepository.findAll(pageable))
        .willReturn(Pagination.getPage(Lists.newArrayList(supplyPartner)));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1));

    assertResponseBody(response, "content[0]", is(supplyPartner.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInGetSupplyPartners() {
    restAssured
        .given()
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInGetSupplyPartners() {
    mockUserHasNoRight(RightName.SUPPLY_PARTNER_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSupplyPartner() {
    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(supplyPartnerDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_CREATED);

    assertResponseBody(response, "", is(not(supplyPartnerDto.getId().toString())));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestBodyIsInvalidInPostSupplyPartners() {
    supplyPartnerDto.getAssociations().get(0).setFacilities(Lists.newArrayList());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(supplyPartnerDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(SupplyPartnerMessageKeys.ERROR_MISSING_FACILITIES));

    // we don't check request body because of the purpose of the test
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.validates());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPostSupplyPartners() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(supplyPartnerDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInPostSupplyPartners() {
    mockUserHasNoRight(RightName.SUPPLY_PARTNER_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(supplyPartnerDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupplyPartner() {
    given(supplyPartnerRepository.findOne(supplyPartner.getId())).willReturn(supplyPartner);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supplyPartnerDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    assertResponseBody(response, "", is(supplyPartner.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInGetSupplyPartner() {
    restAssured
        .given()
        .pathParam(ID, supplyPartnerDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInGetSupplyPartner() {
    mockUserHasNoRight(RightName.SUPPLY_PARTNER_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supplyPartnerDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfSupplyPartnerDoesNotExistInGetSupplyPartner() {
    given(supplyPartnerRepository.findOne(any(UUID.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supplyPartnerDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(SupplyPartnerMessageKeys.ERROR_NOT_FOUND_WITH_ID));


    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupplyPartner() {
    given(supplyPartnerRepository.findOne(supplyPartner.getId())).willReturn(supplyPartner);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, supplyPartnerDto.getId())
        .body(supplyPartnerDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    assertResponseBody(response, "", is(supplyPartner.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestIsInvalidInPutSupplyPartner() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, UUID.randomUUID())
        .body(supplyPartnerDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(SupplyPartnerMessageKeys.ERROR_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPutSupplyPartner() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, supplyPartnerDto.getId())
        .body(supplyPartnerDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInPutSupplyPartner() {
    mockUserHasNoRight(RightName.SUPPLY_PARTNER_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, supplyPartnerDto.getId())
        .body(supplyPartnerDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAuditLog() {
    given(supplyPartnerRepository.findOne(any(UUID.class))).willReturn(supplyPartner);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInGetAuditLog() {
    mockUserHasNoRight(RightName.SUPPLY_PARTNER_MANAGE);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfSupplyPartnerDoesNotExistInGetAuditLog() {
    given(supplyPartnerRepository.findOne(supplyPartner.getId())).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void assertResponseBody(ValidatableResponse response, String resourcePath,
      Matcher<String> idMatcher) {
    response
        .rootPath(resourcePath)
        .body(ID, idMatcher)
        .body("name", is(supplyPartnerDto.getName()))
        .body("code", is(supplyPartnerDto.getCode()))
        .body("associations", hasSize(1))
        .body("associations[0].program.id", is(program.getId().toString()))
        .body("associations[0].supervisoryNode.id", is(supervisoryNode.getId().toString()))
        .body("associations[0].facilities", hasSize(1))
        .body("associations[0].facilities.id", hasItem(facility.getId().toString()))
        .body("associations[0].orderables", hasSize(1))
        .body("associations[0].orderables.id", hasItem(orderable.getId().toString()));
  }

}

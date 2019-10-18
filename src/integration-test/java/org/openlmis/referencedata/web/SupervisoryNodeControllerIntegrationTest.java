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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys.ERROR_NAME_REQUIRED;
import static org.openlmis.referencedata.web.SupervisoryNodeSearchParams.CODE_PARAM;
import static org.openlmis.referencedata.web.SupervisoryNodeSearchParams.FACILITY_ID;
import static org.openlmis.referencedata.web.SupervisoryNodeSearchParams.NAME_PARAM;
import static org.openlmis.referencedata.web.SupervisoryNodeSearchParams.PROGRAM_ID;
import static org.openlmis.referencedata.web.SupervisoryNodeSearchParams.ZONE_ID;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openlmis.referencedata.AvailableFeatures;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ObjectReferenceDto;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.RequisitionGroupDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.togglz.junit.TogglzRule;

@SuppressWarnings({"PMD.TooManyMethods"})
public class SupervisoryNodeControllerIntegrationTest extends BaseWebIntegrationTest {

  @Rule
  public TogglzRule togglzRule = TogglzRule.builder(AvailableFeatures.class)
      .disable(AvailableFeatures.REDIS_CACHING)
      .build();

  private static final String RESOURCE_URL = "/api/supervisoryNodes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SUPERVISING_USERS_URL = ID_URL + "/supervisingUsers";
  private static final String SUPERVISING_FACILITIES_URL = ID_URL + "/facilities";
  private static final String RIGHT_ID_PARAM = "rightId";
  private static final String DESCRIPTION = "OpenLMIS";

  private SupervisoryNode supervisoryNode;
  private SupervisoryNodeDto supervisoryNodeDto;
  private UUID supervisoryNodeId;
  private Facility facility;
  private Program program;
  private Right right;
  private UUID facilityId;
  private UUID programId;
  private UUID rightId;
  private UUID zoneId;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    facility = new FacilityDataBuilder().build();

    supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .withRequisitionGroup(new RequisitionGroupDataBuilder().build())
        .withParentNode(new SupervisoryNodeDataBuilder().build())
        .withChildNode(new SupervisoryNodeDataBuilder().build())
        .build();

    supervisoryNodeDto = new SupervisoryNodeDto();
    supervisoryNodeDto.setServiceUrl(baseUri);
    supervisoryNode.export(supervisoryNodeDto);
    supervisoryNodeId = supervisoryNode.getId();

    program = new ProgramDataBuilder().build();

    right = Right.newRight("right1", RightType.SUPERVISION);

    facilityId = facility.getId();
    programId = UUID.randomUUID();
    rightId = UUID.randomUUID();
    zoneId = UUID.randomUUID();

    when(facilityRepository.findOne(supervisoryNodeDto.getFacilityId()))
        .thenReturn(facility);
    when(requisitionGroupRepository.findOne(supervisoryNodeDto.getRequisitionGroupId()))
        .thenReturn(supervisoryNode.getRequisitionGroup());
    when(supervisoryNodeRepository.findOne(supervisoryNodeDto.getParentNodeId()))
        .thenReturn(supervisoryNode.getParentNode());
    when(supervisoryNodeRepository.findAll(supervisoryNodeDto.getChildNodeIds()))
        .thenReturn(Lists.newArrayList(supervisoryNode.getChildNodes()));

    when(supervisoryNodeRepository.save(any(SupervisoryNode.class)))
        .thenAnswer(new SaveAnswer<>());
  }

  @Test
  public void shouldDeleteSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteSupervisoryNodeFromCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.findById(supervisoryNodeId))
        .willReturn(supervisoryNodeDto);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    verify(supervisoryNodeDtoRedisRepository, times(1))
        .delete(supervisoryNodeDto);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    assertNotNull(supervisoryNodeDto.getRequisitionGroup());

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertResponseBody(response, is(notNullValue(String.class)));
    assertEquals(supervisoryNodeDto.getId(), supervisoryNodeId);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupervisoryNodeWithoutRequisitionGroup() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
            .withoutId()
            .withFacility(supervisoryNode.getFacility())
            .withoutRequisitionGroup()
            .withParentNode(supervisoryNode.getParentNode());

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
            .build()
            .export(supervisoryNodeDto);

    SupervisoryNodeDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(supervisoryNodeDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(201)
            .extract().as(SupervisoryNodeDto.class);

    assertNull(supervisoryNodeDto.getId());
    assertNull(supervisoryNodeDto.getRequisitionGroup());
    assertNull(response.getRequisitionGroup());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupervisoryNodeWithRequisitionGroup() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
            .withoutId()
            .withFacility(supervisoryNode.getFacility())
            .withRequisitionGroup(supervisoryNode.getRequisitionGroup())
            .withParentNode(supervisoryNode.getParentNode());

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
            .build()
            .export(supervisoryNodeDto);

    SupervisoryNodeDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(supervisoryNodeDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(201)
            .extract().as(SupervisoryNodeDto.class);

    assertNull(supervisoryNodeDto.getId());
    assertNotNull(response.getRequisitionGroup());
    assertEquals(supervisoryNodeDto.getRequisitionGroupId(), response.getRequisitionGroupId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAddRequisitionGroupToExistingSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
            .withId(supervisoryNodeId)
            .withFacility(supervisoryNode.getFacility())
            .withoutRequisitionGroup()
            .withParentNode(supervisoryNode.getParentNode());

    SupervisoryNode existing = builder.build();

    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(existing);
    given(supervisoryNodeRepository.findByCode(existing.getCode())).willReturn(existing);

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
            .withRequisitionGroup(supervisoryNode.getRequisitionGroup())
            .build()
            .export(supervisoryNodeDto);

    SupervisoryNodeDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", supervisoryNodeId)
            .body(supervisoryNodeDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(200)
            .extract().as(SupervisoryNodeDto.class);

    assertEquals(supervisoryNodeDto.getRequisitionGroupId(), response.getRequisitionGroupId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(supervisoryNodeRepository.findOne(supervisoryNodeId).getRequisitionGroup());
  }

  @Test
  public void shouldRejectPostSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostSupervisoryNodeIfCodeIsMissing() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
    supervisoryNodeDto.setCode(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostSupervisoryNodeIfCodeIsDuplicated() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNode existing = new SupervisoryNodeDataBuilder()
        .withCode(supervisoryNode.getCode())
        .build();

    given(supervisoryNodeRepository.findByCode(supervisoryNode.getCode())).willReturn(existing);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostSupervisoryNodeIfNameIsNull() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
    supervisoryNodeDto.setName(null);

    String messageKey = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(supervisoryNodeDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(400)
            .extract()
            .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_NAME_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotRejectPostSupervisoryNodeIfRequisitionGroupIsMissing() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    supervisoryNodeDto.setRequisitionGroup((ObjectReferenceDto) null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    supervisoryNodeDto.setDescription(DESCRIPTION);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    ValidatableResponse response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", supervisoryNodeId)
            .body(supervisoryNodeDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(200);

    assertResponseBody(response, is(supervisoryNodeDto.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupervisoryNodeAndDeleteOneFromCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    supervisoryNodeDto.setDescription(DESCRIPTION);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.findById(supervisoryNodeId))
        .willReturn(supervisoryNodeDto);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNodeDto.getId().toString()));
    verify(supervisoryNodeDtoRedisRepository,
        times(1)).delete(supervisoryNodeDto);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupervisoryNodeWhenRequisitionGroupIsChanged() {
    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
        .withFacility(supervisoryNode.getFacility())
        .withRequisitionGroup(supervisoryNode.getRequisitionGroup())
        .withParentNode(supervisoryNode.getParentNode());

    SupervisoryNode existing = builder.build();

    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(existing);
    given(supervisoryNodeRepository.findByCode(existing.getCode())).willReturn(existing);

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
        .withRequisitionGroup(new RequisitionGroupDataBuilder().build())
        .build()
        .export(supervisoryNodeDto);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupervisoryNode() {
    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
            .withFacility(supervisoryNode.getFacility())
            .withRequisitionGroup(supervisoryNode.getRequisitionGroup())
            .withParentNode(supervisoryNode.getParentNode());

    supervisoryNode
            .getChildNodes()
            .forEach(builder::withChildNode);

    SupervisoryNode existing = builder.build();

    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(existing);
    given(supervisoryNodeRepository.findByCode(existing.getCode())).willReturn(existing);

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
            .build()
            .export(supervisoryNodeDto);

    ValidatableResponse response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", supervisoryNodeId)
            .body(supervisoryNodeDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(200);

    assertResponseBody(response, is(supervisoryNodeDto.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupervisoryNodeIfRequisitionGroupIsNotOnUpdateOnly() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder();

    SupervisoryNodeDto dto = new SupervisoryNodeDto();
    builder
        .withId(supervisoryNodeId)
        .withoutRequisitionGroup()
        .build()
        .export(dto);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", supervisoryNodeId)
            .body(dto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(400);

    assertEquals(supervisoryNodeDto.getId(), dto.getId());
    assertNotNull(dto.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAcceptPutSupervisoryNodeIfRequisitionGroupIsNotOnBothUpdateAndExistingNode() {
    SupervisoryNodeDataBuilder builder = new SupervisoryNodeDataBuilder()
            .withFacility(supervisoryNode.getFacility())
            .withoutRequisitionGroup()
            .withParentNode(supervisoryNode.getParentNode());

    SupervisoryNode existing = builder.build();

    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(existing);
    given(supervisoryNodeRepository.findByCode(existing.getCode())).willReturn(existing);

    supervisoryNodeDto = new SupervisoryNodeDto();
    builder
            .withoutRequisitionGroup()
            .build()
            .export(supervisoryNodeDto);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", supervisoryNodeId)
            .body(supervisoryNodeDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupervisoryNodeIfCodeIsMissing() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
    supervisoryNodeDto.setCode(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupervisoryNodeIfCodeIsDuplicated() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    SupervisoryNode existing = new SupervisoryNodeDataBuilder()
        .withCode(supervisoryNode.getCode())
        .build();

    given(supervisoryNodeRepository.findByCode(supervisoryNode.getCode())).willReturn(existing);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupervisoryNodeFromDatabaseWhenNotInCache() {

    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(false);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNode.getId().toString()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveSupervisoryNodeInCacheAfterGettingOneFromDatabase() {

    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(false);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNode.getId().toString()));
    verify(supervisoryNodeDtoRedisRepository, times(1)).save(supervisoryNodeDto);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupervisoryNodeFromCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeDtoRedisRepository.findById(supervisoryNodeId))
        .willReturn(supervisoryNodeDto);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNodeId.toString()));
    verify(supervisoryNodeDtoRedisRepository, times(1))
        .findById(supervisoryNodeId);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldThrowErrorNotFoundWhenNeitherInDatabaseNorInCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(false);
    given(supervisoryNodeDtoRedisRepository.exists(supervisoryNodeId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupervisoryNodeFromDatabase() {
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNode.getId().toString()));
    verifyZeroInteractions(supervisoryNodeDtoRedisRepository);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSaveSupervisoryNodeInCacheWhenFeatureFlagDisabled() {
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200);

    assertResponseBody(response, is(supervisoryNode.getId().toString()));
    verifyZeroInteractions(supervisoryNodeDtoRedisRepository);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldThrowErrorNotFoundWhenInDatabase() {
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldGetSupervisingUsers() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    Role role = Role.newRole("role", right);
    role.setId(UUID.randomUUID());

    User supervisingUser = new UserDataBuilder().build();
    supervisingUser.assignRoles(
        new SupervisionRoleAssignment(role, supervisingUser, program, supervisoryNode));

    Set<User> supervisingUsers = asSet(supervisingUser);

    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(rightRepository.exists(rightId)).willReturn(true);
    given(programRepository.exists(programId)).willReturn(true);
    given(userRepository.findUsersBySupervisionRight(rightId, supervisoryNodeId, programId))
        .willReturn(supervisingUsers);

    UserDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    assertThat(response.length, is(1));
    assertEquals(supervisingUser.getUsername(), response[0].getUsername());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnBadRequestIfRightNotFound() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(rightRepository.exists(rightId)).willReturn(false);
    given(programRepository.exists(programId)).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnBadRequestIfProgramNotFound() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(true);
    given(rightRepository.exists(rightId)).willReturn(true);
    given(programRepository.exists(programId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USERS_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnNotFoundIfSupervisoryNodeNotFound() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(false);
    given(rightRepository.exists(rightId)).willReturn(true);
    given(programRepository.exists(programId)).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchSupervisoryNodes() {
    final UUID id = UUID.randomUUID();

    final Pageable pageable = new PageRequest(0, 10);

    HashMap<String, Object> queryParams = new HashMap<>();

    queryParams.put(FACILITY_ID, facilityId.toString());
    queryParams.put(PROGRAM_ID, programId.toString());
    queryParams.put(ZONE_ID, zoneId);
    queryParams.put(NAME_PARAM, "some-name");
    queryParams.put(CODE_PARAM, "some-code");
    queryParams.put(ID, id);
    queryParams.put(PAGE, 0);
    queryParams.put(SIZE, 10);

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(
        "some-name", "some-code", facilityId, programId, zoneId, asSet(id));
    given(supervisoryNodeRepository.search(params, pageable))
        .willReturn(new PageImpl(Collections.singletonList(supervisoryNode), pageable, 1));

    PageImplRepresentation response = searchForSupervisoryNode(queryParams, 200)
        .extract().as(PageImplRepresentation.class);
    Map<String, String> foundSupervisoryNode = (LinkedHashMap) response.getContent().get(0);

    assertEquals(supervisoryNode.getCode(), foundSupervisoryNode.get("code"));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    HashMap<String, Object> queryParams = new HashMap<>();

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParams(queryParams)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(supervisoryNodeRepository.findOne(any(UUID.class))).willReturn(supervisoryNode);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnSupervisingFacilities() {
    SupervisoryNode spy = spy(supervisoryNode);
    given(spy.getAllSupervisedFacilities(program)).willReturn(Sets.newHashSet(facility));

    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(spy);
    given(programRepository.findOne(programId)).willReturn(program);
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supervisoryNodeId)
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISING_FACILITIES_URL)
        .then()
        .statusCode(200)
        .body("content", hasSize(1))
        .body("content[0].id", is(facility.getId().toString()));
  }

  @Test
  public void shouldReturnUnauthorizedErrorIfTokenWasNotProvidedToGetSupervisingFacilities() {
    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, supervisoryNodeId)
        .when()
        .get(SUPERVISING_FACILITIES_URL)
        .then()
        .statusCode(401);
  }

  @Test
  public void shouldReturnForbiddenErrorIfUserDoesNotHaveRightToGetSupervisingFacilities() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supervisoryNodeId)
        .when()
        .get(SUPERVISING_FACILITIES_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));
  }

  @Test
  public void shouldThrowNotFoundErrorIfSupervisoryNodeDoesNotExistForGetSupervisingFacilities() {
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(null);
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supervisoryNodeId)
        .when()
        .get(SUPERVISING_FACILITIES_URL)
        .then()
        .statusCode(404)
        .body(MESSAGE_KEY, is(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND));
  }

  @Test
  public void shouldThrowNotFoundErrorIfProgramDoesNotExistForGetSupervisingFacilities() {
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(programRepository.findOne(any(UUID.class))).willReturn(null);
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, supervisoryNodeId)
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .when()
        .get(SUPERVISING_FACILITIES_URL)
        .then()
        .statusCode(404)
        .body(MESSAGE_KEY, is(ProgramMessageKeys.ERROR_NOT_FOUND));
  }

  private ValidatableResponse searchForSupervisoryNode(HashMap<String, Object> queryParams,
      int expectedCode) {
    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParams(queryParams)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(expectedCode);
  }

  private void assertResponseBody(ValidatableResponse response, Matcher<String> idMatcher) {
    response
        .body(ID, idMatcher)
        .body("code", is(supervisoryNodeDto.getCode()))
        .body("name", is(supervisoryNodeDto.getName()))
        .body("description", is(supervisoryNodeDto.getDescription()))
        .body("facility.id", is(supervisoryNodeDto.getFacilityId().toString()))
        .body("parentNode.id", is(supervisoryNodeDto.getParentNodeId().toString()))
        .body("requisitionGroup.id", is(supervisoryNodeDto.getRequisitionGroupId().toString()))
        .body("childNodes.id", hasItems(supervisoryNodeDto.getChildNodeIds()
            .stream().map(Objects::toString).toArray(String[]::new)));
  }
}

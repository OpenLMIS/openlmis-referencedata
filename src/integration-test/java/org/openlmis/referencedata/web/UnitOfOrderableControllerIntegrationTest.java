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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.UNIT_OF_ORDERABLES_MANAGE;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.UnitOfOrderableBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class UnitOfOrderableControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = UnitOfOrderableController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String ID = "id";
  private static final String NAME = "testUnit";
  private static final int FACTOR = 10;
  private static final String DESCRIPTION = "description";
  private static final int DISPLAY_ORDER = 1;
  public static final String NEW_DESCRIPTION = "new description";

  private UnitOfOrderable unitOfOrderable;
  private UnitOfOrderableDto unitOfOrderableDto = new UnitOfOrderableDto();

  private UUID unitOfOrderableId = UUID.randomUUID();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    unitOfOrderable = new UnitOfOrderableBuilder()
        .withDescription(DESCRIPTION)
        .withDisplayOrder(DISPLAY_ORDER)
        .withName(NAME)
        .withFactor(FACTOR)
        .build();

    unitOfOrderable.setId(unitOfOrderableId);
    unitOfOrderable.export(unitOfOrderableDto);

    when(unitOfOrderableRepository.save(any(UnitOfOrderable.class))).thenReturn(unitOfOrderable);
    when(unitOfOrderableRepository.findById(unitOfOrderableId))
        .thenReturn(Optional.of(unitOfOrderable));
  }

  @Test
  public void shouldCreateNewUnitOfOrderable() {
    mockUserHasRight(UNIT_OF_ORDERABLES_MANAGE);

    Response response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(unitOfOrderableDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().response();

    UnitOfOrderableDto unitOfOrderableDtoResponse = response.as(UnitOfOrderableDto.class);
    assertEquals(unitOfOrderableDto.getDescription(), unitOfOrderableDtoResponse.getDescription());
    assertEquals(unitOfOrderableDto.getDisplayOrder(),
        unitOfOrderableDtoResponse.getDisplayOrder());
    assertEquals(unitOfOrderableDto.getFactor(), unitOfOrderableDtoResponse.getFactor());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateUnitOfOrderable() {
    mockUserHasRight(UNIT_OF_ORDERABLES_MANAGE);
    unitOfOrderableDto.setDescription(NEW_DESCRIPTION);
    when(unitOfOrderableRepository.findById(unitOfOrderableId))
        .thenReturn(Optional.of(unitOfOrderable));

    UnitOfOrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, unitOfOrderableId)
        .body(unitOfOrderableDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(UnitOfOrderableDto.class);

    assertEquals(unitOfOrderableDto.getDescription(), response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAllUnitOfOrderables() {
    final List<UnitOfOrderable> items = Collections.singletonList(unitOfOrderable);
    when(unitOfOrderableRepository
        .findAll(any(Pageable.class)))
        .thenReturn(Pagination.getPage(items, PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    checkIfEquals(response, UnitOfOrderableDto.newInstances(items));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindUnitOfOrderableById() {
    UnitOfOrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, unitOfOrderableId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(UnitOfOrderableDto.class);

    assertEquals(unitOfOrderableId, response.getId());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.UNIT_OF_ORDERABLES_MANAGE);
    when(unitOfOrderableRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(unitOfOrderable));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteUnitOfOrderable() {
    mockUserHasRight(UNIT_OF_ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, unitOfOrderableId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void checkIfEquals(PageDto response, List<UnitOfOrderableDto> expected) {
    List<LinkedHashMap> pageContent = response.getContent();
    assertEquals(expected.size(), pageContent.size());
    for (int i = 0; i < pageContent.size(); i++) {
      Map<String, String> retrieved = (LinkedHashMap) pageContent.get(i);
      UnitOfOrderableDto unitOfOrderableDto = expected.get(i);
      assertEquals(unitOfOrderableDto.getDisplayOrder(),
          retrieved.get("displayOrder"));
      assertEquals(unitOfOrderableDto.getDescription(),
          retrieved.get("description"));
      assertEquals(unitOfOrderableDto.getName(),
          retrieved.get("name"));
      assertEquals(unitOfOrderableDto.getFactor().intValue(),
          retrieved.get("factor"));
    }
  }
}

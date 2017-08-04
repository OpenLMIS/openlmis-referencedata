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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_DUPLICATED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_NET_CONTENT_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PRODUCT_CODE_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_ROUND_TO_ZERO_REQUIRED;

import com.google.common.collect.ImmutableMap;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.DispensableDto;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderables";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String UNIT = "unit";
  private static final String NAME = "name";
  private static final String CODE = "code";
  private static final String PROGRAM_CODE = "program";
  private static final String IDS = "ids";

  private OrderableDto orderableDto;

  private Orderable orderable;

  @Before
  public void setUp() {
    orderableDto = new OrderableDto("code", new DispensableDto(UNIT), NAME, "description", 0L, 0L,
        false, Collections.emptySet(), null, null);

    orderable = new Orderable(Code.code("abcd"), Dispensable.createNew("each"),
        "Abcd", "description", 10, 5, false, Collections.emptySet(), null, null);

    when(orderableRepository.save(any(Orderable.class)))
        .thenAnswer(new SaveAnswer<CommodityType>());
  }

  @Test
  public void shouldCreateNewOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);

    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    assertEquals(orderableDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewOrderableWithProgramOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);
    ProgramOrderableDto programOrderable = generateProgramOrderable();
    orderableDto.setPrograms(Collections.singleton(programOrderable));

    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    assertEquals(orderableDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewOrderableWithIdentifiers() {
    mockUserHasRight(ORDERABLES_MANAGE);
    orderableDto.setIdentifiers(
        ImmutableMap.of(
            "CommodityType", UUID.randomUUID().toString(),
            "TradeItem", UUID.randomUUID().toString()));

    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    assertEquals(orderableDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutOrderableIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectIfProductCodeIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);
    orderableDto.setProductCode("");

    checkBadRequestBody(orderableDto, ERROR_PRODUCT_CODE_REQUIRED, RESOURCE_URL);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectIfPackRoundingThresholdIsNull() {
    mockUserHasRight(ORDERABLES_MANAGE);
    orderableDto.setPackRoundingThreshold(null);

    checkBadRequestBody(orderable, ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED, RESOURCE_URL);
  }

  @Test
  public void shouldRejectIfRoundToZeroIsNull() {
    mockUserHasRight(ORDERABLES_MANAGE);
    orderableDto.setRoundToZero(null);

    checkBadRequestBody(orderableDto, ERROR_ROUND_TO_ZERO_REQUIRED, RESOURCE_URL);
  }

  @Test
  public void shouldRejectIfNetContentIsNull() {
    mockUserHasRight(ORDERABLES_MANAGE);
    orderableDto.setNetContent(null);

    checkBadRequestBody(orderableDto, ERROR_NET_CONTENT_REQUIRED, RESOURCE_URL);
  }

  @Test
  public void shouldRejectIfProductCodeDuplicated() {
    mockUserHasRight(ORDERABLES_MANAGE);

    when(orderableRepository.existsByProductCode(Code.code(orderableDto.getProductCode())))
        .thenReturn(true);

    checkBadRequestBody(orderableDto, ERROR_DUPLICATED, RESOURCE_URL);
  }

  @Test
  public void shouldRetrieveAllOrderables() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<OrderableDto> items = Collections.singletonList(orderableDto);

    when(orderableRepository.findAll()).thenReturn(Orderable.newInstance(items));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    checkIfEquals(response, items);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchOrderables() {
    final String code = "some-code";
    final String name = "some-name";
    final String programCode = "program-code";
    final List<Orderable> items = Collections.singletonList(orderable);

    mockUserHasRight(ORDERABLES_MANAGE);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CODE, code);
    requestBody.put(NAME, name);
    requestBody.put(PROGRAM_CODE, programCode);
    requestBody.put(IDS, orderable.getId());

    when(orderableService.searchOrderables(requestBody)).thenReturn(items);

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    checkIfEquals(response, OrderableDto.newInstance(items));
  }

  @Test
  public void shouldPaginateSearchOrderables() {
    final String code = "some-code";
    final String name = "some-name";
    final String programCode = "program-code";
    final List<Orderable> items = Collections.singletonList(orderable);

    mockUserHasRight(ORDERABLES_MANAGE);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CODE, code);
    requestBody.put(NAME, name);
    requestBody.put(PROGRAM_CODE, programCode);
    requestBody.put(IDS, orderable.getId());

    when(orderableService.searchOrderables(requestBody)).thenReturn(items);

    PageImplRepresentation response = restAssured
        .given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getSize());
    assertEquals(0, response.getNumber());
  }

  private ProgramOrderableDto generateProgramOrderable() {
    return new ProgramOrderableDto(UUID.randomUUID(), UUID.randomUUID(),
        null, null, true, true, 0, 1, Money.of(CurrencyUnit.USD, 10.0));
  }

  private void checkIfEquals(PageImplRepresentation response, List<OrderableDto> expected) {
    List pageContent = response.getContent();
    assertEquals(expected.size(), pageContent.size());
    for (int i = 0; i < pageContent.size(); i++) {
      Map<String, String> retrieved = (LinkedHashMap) pageContent.get(i);
      assertEquals(expected.get(i).getFullProductName(),
          retrieved.get("fullProductName"));
      assertEquals(expected.get(i).getProductCode(),
          retrieved.get("productCode"));
      assertEquals(expected.get(i).getNetContent().intValue(),
          retrieved.get("netContent"));
      assertEquals(expected.get(i).getPackRoundingThreshold().intValue(),
          retrieved.get("packRoundingThreshold"));
      assertEquals(expected.get(i).getRoundToZero(),
          retrieved.get("roundToZero"));
    }
  }

}

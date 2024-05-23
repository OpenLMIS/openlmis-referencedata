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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_ID_MISMATCH;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_NET_CONTENT_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PRODUCT_CODE_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_ROUND_TO_ZERO_REQUIRED;
import static org.openlmis.referencedata.web.BaseController.RFC_7231_FORMAT;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.apache.http.HttpStatus;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.OrderableChildDto;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.PriceChangeDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.dto.VersionIdentityDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.UnitOfOrderableBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderables";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private static final String UNIT = "unit";
  private static final String NAME = "name";
  private static final String CODE = "code";
  private static final String PROGRAM_CODE = "program";
  private static final String ID = "id";
  private static final String VERSION_NAME = "versionNumber";
  private static final String GMT = "GMT";
  private static final String UNIT_OF_ORDERABLE_NAME = "testUnit";
  private static final int UNIT_OF_ORDERABLE_FACTOR = 10;

  @Captor
  public ArgumentCaptor<QueryOrderableSearchParams> searchParamsArgumentCaptor;

  private Orderable orderable;
  private OrderableDto orderableDto = new OrderableDto();

  private UUID orderableId = UUID.randomUUID();
  private Long orderableVersionNumber = 1L;
  private ZonedDateTime modifiedDate = ZonedDateTime.now(ZoneId.of(GMT)).withNano(0);

  @Before
  @Override
  public void setUp() {
    super.setUp();

    UnitOfOrderable unitOfOrderable = new UnitOfOrderableBuilder()
        .withName(UNIT_OF_ORDERABLE_NAME)
        .withFactor(UNIT_OF_ORDERABLE_FACTOR)
        .build();

    List<UnitOfOrderable> units = new ArrayList<>();
    units.add(unitOfOrderable);

    orderable = new OrderableDataBuilder()
        .withProductCode(Code.code(CODE))
        .withDispensable(Dispensable.createNew(UNIT))
        .withProgramOrderables(Collections.emptyList())
        .withVersionNumber(orderableVersionNumber)
        .withUnits(units)
        .build();
    orderable.setId(orderableId);
    orderable.setLastUpdated(modifiedDate);
    orderable.export(orderableDto);

    when(orderableRepository.save(any(Orderable.class))).thenReturn(orderable);
    given(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionNumberDesc(
        orderable.getId())).willReturn(orderable);
  }

  @Test
  public void shouldCreateNewOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);

    Response response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(orderableDto)
            .when()
            .put(RESOURCE_URL)
            .then()
            .statusCode(200)
            .extract().response();

    OrderableDto orderableDtoResponse = response.as(OrderableDto.class);
    assertOrderablesDtoParams(orderableDto, orderableDtoResponse);
    assertEquals(orderableDto.getPrograms(), orderableDtoResponse.getPrograms());
    assertEquals(orderableDto.getUnits(), orderableDtoResponse.getUnits());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response.getHeaders().hasHeaderWithName(HttpHeaders.LAST_MODIFIED), is(true));
  }

  @Test
  public void updateShouldUpdateOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(orderableRepository.save(any(Orderable.class))).thenAnswer(i -> i.getArguments()[0]);

    Response response1 = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().response();

    OrderableDto orderableDto1 = response1.as(OrderableDto.class);
    orderableDto1.setNetContent(11L);

    Response response2 = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto1)
        .when()
        .put(String.join("/", RESOURCE_URL, orderableDto1.getId().toString()))
        .then()
        .statusCode(200)
        .extract().response();

    OrderableDto orderableDto2 = response2.as(OrderableDto.class);

    assertEquals(11L, orderableDto2.getNetContent().longValue());
    assertEquals(orderableDto1.getId(), orderableDto2.getId());
    assertNotEquals(orderableDto1, orderableDto2);
    assertThat(response1.getHeaders().hasHeaderWithName(HttpHeaders.LAST_MODIFIED), is(true));
    assertThat(response2.getHeaders().hasHeaderWithName(HttpHeaders.LAST_MODIFIED), is(true));
  }

  @Test
  public void updateShouldUpdateOrderableWithChildOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(orderableRepository.save(any(Orderable.class))).thenAnswer(i -> i.getArguments()[0]);

    Response response1 = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(orderableDto)
            .when()
            .put(RESOURCE_URL)
            .then()
            .statusCode(200)
            .extract().response();

    OrderableDto orderableDto1 = response1.as(OrderableDto.class);
    orderableDto1.setNetContent(11L);
    Orderable orderableChild =
        new OrderableDataBuilder()
            .withProductCode(Code.code(CODE))
            .withDispensable(Dispensable.createNew(UNIT))
            .withVersionNumber(orderableVersionNumber)
            .withProgramOrderables(Collections.emptyList())
            .build();
    orderableChild.setLastUpdated(modifiedDate);

    OrderableDto orderableChildDto = new OrderableDto();
    orderableChild.export(orderableChildDto);
    orderableChildDto.setNetContent(11L);
    List<UUID> orderableIdList = new ArrayList<>();
    orderableIdList.add(orderableChildDto.getId());

    List<Orderable> orderableList = new ArrayList<>();
    orderableList.add(orderableChild);
    Page<Orderable> orderableMap = new PageImpl<Orderable>(orderableList);

    when(orderableRepository.findAllLatestByIds(orderableIdList, null)).thenReturn(orderableMap);


    Response response2 = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableChildDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().response();



    OrderableDto orderableDto2 = response2.as(OrderableDto.class);
    orderableDto2.setNetContent(11L);

    OrderableChildDto childDto = new OrderableChildDto();
    childDto.setOrderable(orderableChild);
    childDto.setQuantity(1L);
    Set<OrderableChildDto> childSet = new HashSet<>();
    childSet.add(childDto);

    orderableDto1.setChildren(childSet);

    Response response3 = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(orderableDto1)
            .when()
            .put(String.join("/", RESOURCE_URL, orderableDto.getId().toString()))
            .then()
            .statusCode(200)
            .extract().response();

    OrderableDto orderableDto3 = response3.as(OrderableDto.class);

    assertEquals(11L, orderableDto3.getNetContent().longValue());
    assertEquals(orderableDto1.getId(), orderableDto3.getId());
    assertThat(response1.getHeaders().hasHeaderWithName(HttpHeaders.LAST_MODIFIED), is(true));
    assertThat(response3.getHeaders().hasHeaderWithName(HttpHeaders.LAST_MODIFIED), is(true));
    assertEquals(orderableChild.getId(),
        getChildFromOrderableDto(orderableDto3).getOrderable().getId());
    assertEquals(getChildFromOrderableDto(orderableDto1).getQuantity(),
        getChildFromOrderableDto(orderableDto3).getQuantity());
  }

  @Test
  public void shouldUpdateSequentially() throws Exception {
    int requestCount = 10; //too many requests may slow down test execution
    mockUserHasRight(ORDERABLES_MANAGE);
    mockDecreasingResponseTime(requestCount);

    Queue<String> queue = new ConcurrentLinkedQueue<>();
    ExecutorService executorService = Executors.newFixedThreadPool(java.lang.Thread.activeCount());

    for (int requestNumber = 0; requestNumber < requestCount; requestNumber++) {
      simulateAsyncCreateRequestAndLogToQueue(queue, executorService, requestNumber);
    }

    try {
      executorService.awaitTermination(10L, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      executorService.shutdownNow();
      throw ie;
    }

    assertEquals(requestCount, queue.size());
    assertProperExecutionOrder(requestCount, queue);
    executorService.shutdownNow();
  }

  @Test
  public void updateShouldReturnBadRequestIfUrlAndDtoIdsDoNotMatch() {
    mockUserHasRight(ORDERABLES_MANAGE);

    checkBadRequestBody(orderableDto, ERROR_ID_MISMATCH,
        String.join("/", RESOURCE_URL, UUID.randomUUID().toString()));
  }

  @Test
  public void updateShouldReturnNotFoundIfOrderableNotFound() {
    mockUserHasRight(ORDERABLES_MANAGE);
    given(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionNumberDesc(
        orderable.getId())).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(String.join("/", RESOURCE_URL, orderableDto.getId().toString()))
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewOrderableWithProgramOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);
    UUID programId = UUID.randomUUID();
    Program program = new Program(programId);
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory
        .createNew(Code.code("orderableDisplayCategoryCode"));
    orderableDisplayCategory.setId(UUID.randomUUID());
    ProgramOrderable programOrderable = new ProgramOrderable(program, orderable, 1, true,
        orderableDisplayCategory, true, 1, Money.of(CurrencyUnit.USD, 10.0),
        Collections.emptyList());
    orderable.setProgramOrderables(Collections.singletonList(programOrderable));
    orderable.export(orderableDto);

    when(programRepository.findById(programId)).thenReturn(Optional.of(program));

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

    assertOrderablesDtoParams(orderableDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewOrderableWithIdentifiers() {
    mockUserHasRight(ORDERABLES_MANAGE);
    Map<String, String> identifiersMap = ImmutableMap.of(
        "CommodityType", UUID.randomUUID().toString(),
        "TradeItem", UUID.randomUUID().toString());
    orderable.setIdentifiers(identifiersMap);
    orderable.export(orderableDto);

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

    assertOrderablesDtoParams(orderableDto, response);
    assertEquals(orderableDto.getPrograms(), response.getPrograms());
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

    checkBadRequestBody(orderableDto, ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED, RESOURCE_URL);
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
  public void shouldRetrieveAllOrderables() {
    final List<Orderable> items = Collections.singletonList(orderable);
    when(orderableService
        .searchOrderables(any(QueryOrderableSearchParams.class), any(Pageable.class)))
        .thenReturn(Pagination.getPage(items, PageRequest.of(0, 10)));

    when(orderableService
        .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
        .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(PageDto.class);

    checkIfEquals(response, OrderableDto.newInstance(items));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAllOrderablesIfAnyResourceWasModified() {
    final List<Orderable> items = Collections.singletonList(orderable);
    when(orderableService
        .searchOrderables(any(QueryOrderableSearchParams.class), any(Pageable.class)))
        .thenReturn(Pagination.getPage(items, PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.minusHours(1).format(RFC_7231_FORMAT))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(PageDto.class);

    checkIfEquals(response, OrderableDto.newInstance(items));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnEmptyPageIfNoOrderableWithLastUpdatedDateWasFound() {
    when(orderableService
        .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
        .thenReturn(null);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract().as(PageDto.class);

    checkIfEquals(response, OrderableDto.newInstance(Collections.emptyList()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotModifiedAndNoResponseBodyIfNoOrderableWasModified() {
    final List<Orderable> items = Collections.singletonList(orderable);
    when(orderableService
        .searchOrderables(any(QueryOrderableSearchParams.class), any(Pageable.class)))
        .thenReturn(Pagination.getPage(items, PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.format(RFC_7231_FORMAT))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_MODIFIED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchOrderables() {
    final String code = "some-code";
    final String name = "some-name";
    final String programCode = "program-code";
    final List<Orderable> items = Collections.singletonList(orderable);

    UUID orderableId2 = UUID.randomUUID();

    when(orderableService
        .searchOrderables(any(QueryOrderableSearchParams.class), any(Pageable.class)))
        .thenReturn(Pagination.getPage(items, PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .parameter(CODE, code)
        .parameter(NAME, name)
        .parameter(PROGRAM_CODE, programCode)
        .parameter(ID, orderableId)
        .parameter(ID, orderableId2)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(PageDto.class);

    checkIfEquals(response, OrderableDto.newInstance(items));

    verify(orderableService)
        .searchOrderables(searchParamsArgumentCaptor.capture(), any(Pageable.class));

    QueryOrderableSearchParams value = searchParamsArgumentCaptor.getValue();
    assertEquals(code, value.getCode());
    assertEquals(name, value.getName());
    assertEquals(programCode, value.getProgramCode());
    assertEquals(new HashSet<>(Arrays.asList(orderableId, orderableId2)), value.getIds());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPaginateSearchOrderables() {
    final List<Orderable> items = Collections.singletonList(orderable);

    Pageable page = PageRequest.of(0, 10);
    when(orderableService.searchOrderables(any(QueryOrderableSearchParams.class), eq(page)))
        .thenReturn(Pagination.getPage(items, page));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .queryParam("page", page.getPageNumber())
        .queryParam("size", page.getPageSize())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(10, response.getSize());
    assertEquals(0, response.getNumber());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowPaginationWithZeroSize() {
    final List<Orderable> items = Collections.singletonList(orderable);

    Pageable page = PageRequest.of(0, 0);
    when(orderableService.searchOrderables(any(QueryOrderableSearchParams.class), eq(page)))
        .thenReturn(Pagination.getPage(items, page));

    restAssured
        .given()
        .queryParam("page", page.getPageNumber())
        .queryParam("size", page.getPageSize())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowPaginationWithoutSize() {
    final List<Orderable> items = Collections.singletonList(orderable);

    Pageable page = PageRequest.of(0, 0);
    when(orderableService.searchOrderables(any(QueryOrderableSearchParams.class), eq(page)))
        .thenReturn(Pagination.getPage(items, page));

    restAssured
        .given()
        .queryParam("page", page.getPageNumber())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400);
  }

  @Test
  public void shouldFindOrderableByIdentityId() {
    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, orderableId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(OrderableDto.class);

    assertEquals(orderableId, response.getId());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindOrderableByIdentityIdAndVersionNumber() {
    when(orderableRepository.findByIdentityIdAndIdentityVersionNumber(
        orderable.getId(), orderableVersionNumber)).thenReturn(orderable);

    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, orderableId)
        .queryParam(VERSION_NAME, orderableVersionNumber)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(OrderableDto.class);

    assertEquals(orderableId, response.getId());
    assertEquals(orderableVersionNumber, orderable.getVersionNumber());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnOrderableIfResourceWasModified() {
    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.minusHours(1).format(RFC_7231_FORMAT))
        .pathParam(ID, orderableId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(OrderableDto.class);

    assertEquals(orderableId, response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotModifiedAndNoResponseBodyWhenResourceWasNotModified() {
    when(orderableRepository.findByIdentityIdAndIdentityVersionNumber(
        orderable.getId(), orderableVersionNumber)).thenReturn(orderable);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.format(RFC_7231_FORMAT))
        .pathParam(ID, orderableId)
        .queryParam(VERSION_NAME, orderableVersionNumber)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(304)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /orderables/search
  @Test
  public void shouldPostSearchOrderables() {
    OrderableSearchParams searchParams = new OrderableSearchParams(
        orderableDto.getProductCode(), orderableDto.getFullProductName(),
        orderable.getProductCode().toString(),
        Lists.newArrayList(new VersionIdentityDto(
            orderableDto.getId(), orderableDto.getVersionNumber())),
        false, 0, 10);

    given(orderableRepository
        .search(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(orderable), PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class),
                    any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract()
        .as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(orderableDto.getId().toString(),
            ((java.util.LinkedHashMap) response.getContent().get(0)).get("id"));
  }

  @Test
  public void shouldPostSearchOrderablesIfAnyResourceWasModified() {
    OrderableSearchParams searchParams = new OrderableSearchParams(
        orderableDto.getProductCode(), orderableDto.getFullProductName(),
        orderable.getProductCode().toString(),
        Lists.newArrayList(new VersionIdentityDto(
            orderableDto.getId(), orderableDto.getVersionNumber())),
        false, 0, 10);

    given(orderableRepository
        .search(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(orderable), PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.minusHours(1).format(RFC_7231_FORMAT))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract()
        .as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(orderableDto.getId().toString(),
        ((java.util.LinkedHashMap) response.getContent().get(0)).get("id"));
  }

  @Test
  public void shouldReturnNotModifiedAndNoResponseBodyWhenNoResourcesWereModified() {
    OrderableSearchParams searchParams = new OrderableSearchParams(
        orderableDto.getProductCode(), orderableDto.getFullProductName(),
        orderable.getProductCode().toString(),
        Lists.newArrayList(new VersionIdentityDto(
            orderableDto.getId(), orderableDto.getVersionNumber())),
        false, 0, 10);

    given(orderableRepository
        .search(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(orderable), PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MODIFIED_SINCE, modifiedDate.format(RFC_7231_FORMAT))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_MODIFIED)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postSearchShouldReturnEmptyPageWhenNoOrderablesFound() {
    OrderableSearchParams searchParams = new OrderableSearchParams(
        orderableDto.getProductCode(), orderableDto.getFullProductName(),
        orderable.getProductCode().toString(),
        Lists.newArrayList(new VersionIdentityDto(
            orderableDto.getId(), orderableDto.getVersionNumber())),
        false, 0, 10);

    given(orderableRepository
        .search(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(), PageRequest.of(0, 10)));

    when(orderableService
            .getLatestLastUpdatedDate(any(QueryOrderableSearchParams.class), any(Profiler.class)))
            .thenReturn(modifiedDate);

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .header(HttpHeaders.LAST_MODIFIED, modifiedDate.format(RFC_7231_FORMAT))
        .extract().as(PageDto.class);

    checkIfEquals(response, OrderableDto.newInstance(Collections.emptyList()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPostSearchEndpoint() {
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(new OrderableSearchParams())
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);
    given(orderableRepository.existsById(any(UUID.class))).willReturn(false);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);
    given(orderableRepository.existsById(any(UUID.class))).willReturn(true);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProgramOrderableDto generateProgramOrderable() {
    return new ProgramOrderableDto(UUID.randomUUID(), UUID.randomUUID(),
        null, null, true, true, 0, 1, Money.of(CurrencyUnit.USD, 10.0),
        Collections.singletonList(new PriceChangeDto()));
  }

  private void checkIfEquals(PageDto response, List<OrderableDto> expected) {
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
      assertEquals(expected.get(i).getUnits(),
          retrieved.get("units"));
    }
  }

  private void simulateAsyncCreateRequestAndLogToQueue(Queue<String> queue,
      ExecutorService executorService,
      int requestNumber) throws Exception {
    String requestIndex = String.valueOf(requestNumber);

    Future future = executorService.submit(() -> {
      orderableDto.setDescription(requestIndex);
      restAssured
          .given()
          .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(orderableDto)
          .when()
          .put(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(OrderableDto.class);
      queue.add(requestIndex);
    });

    future.get(); // throws exception if exception in executed task
  }

  private void assertProperExecutionOrder(int requestCount, Queue<String> queue) {
    IntStream.range(0, requestCount).forEach(i -> assertEquals(String.valueOf(i), queue.poll()));
  }

  private void mockDecreasingResponseTime(int requestCount) {
    when(orderableRepository.save(any(Orderable.class))).then((invocation) -> {
      Orderable orderable = (Orderable) invocation.getArguments()[0];
      TimeUnit.MILLISECONDS.sleep(10 * requestCount
          - requestCount * Integer.valueOf(orderable.getDescription()));
      return orderable;
    });
  }
  
  private OrderableChildDto getChildFromOrderableDto(OrderableDto orderableDto) {
    if (orderableDto.getChildren().size() > 0) {
      return orderableDto.getChildren().iterator().next();
    }
    return null;
  }

  private void assertOrderablesDtoParams(OrderableDto orderableDto, OrderableDto response) {
    assertEquals(orderableDto.getProductCode(), response.getProductCode());
    assertEquals(orderableDto.getDispensable(), response.getDispensable());
    assertEquals(orderableDto.getFullProductName(), response.getFullProductName());
    assertEquals(orderableDto.getDescription(), response.getDescription());
    assertEquals(orderableDto.getNetContent(), response.getNetContent());
    assertEquals(orderableDto.getPackRoundingThreshold(), response.getPackRoundingThreshold());
    assertEquals(orderableDto.getRoundToZero(), response.getRoundToZero());
    assertEquals(orderableDto.getChildren(), response.getChildren());
    assertEquals(orderableDto.getIdentifiers(), response.getIdentifiers());
    assertEquals(orderableDto.getExtraData(), response.getExtraData());
    assertEquals(orderableDto.getOrderableRepository(), response.getOrderableRepository());
    assertEquals(orderableDto.getVersionNumber(), response.getVersionNumber());
    assertEquals(orderableDto.isQuarantined(), response.isQuarantined());
  }

}

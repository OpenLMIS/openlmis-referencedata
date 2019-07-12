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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.dto.VersionIdentityDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
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
  private static final String VERSION_ID = "versionId";

  @Captor
  public ArgumentCaptor<QueryOrderableSearchParams> searchParamsArgumentCaptor;

  private Orderable orderable;
  private OrderableDto orderableDto = new OrderableDto();

  private UUID orderableId = UUID.randomUUID();
  private Long orderableVersionId = 1L;

  @Before
  @Override
  public void setUp() {
    super.setUp();

    orderable = new Orderable(Code.code(CODE), Dispensable.createNew(UNIT),
        10, 5, false, orderableId, orderableVersionId);
    orderable.setProgramOrderables(Collections.emptyList());
    orderable.setLastUpdated(ZonedDateTime.now(ZoneId.of("UTC")));
    orderable.export(orderableDto);

    when(orderableRepository.save(any(Orderable.class))).thenReturn(orderable);
    given(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionIdDesc(orderable.getId()))
        .willReturn(orderable);
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
  public void updateShouldUpdateOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(orderableRepository.save(any(Orderable.class))).thenAnswer(i -> i.getArguments()[0]);

    OrderableDto response1 = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    response1.setNetContent(11L);

    OrderableDto response2 = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(response1)
        .when()
        .put(String.join("/", RESOURCE_URL, response1.getId().toString()))
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    assertEquals(response1.getId(), response2.getId());
    assertEquals(11L, response2.getNetContent().longValue());
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
    given(orderableRepository.findFirstByIdentityIdOrderByIdentityVersionIdDesc(orderable.getId()))
        .willReturn(null);

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
        orderableDisplayCategory, true, 1, Money.of(CurrencyUnit.USD, 10.0));
    orderable.setProgramOrderables(Collections.singletonList(programOrderable));
    orderable.export(orderableDto);

    when(programRepository.findOne(programId)).thenReturn(program);

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
        .thenReturn(Pagination.getPage(items));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    checkIfEquals(response, OrderableDto.newInstance(items));

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
        .thenReturn(Pagination.getPage(items));

    PageImplRepresentation response = restAssured
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
        .extract().as(PageImplRepresentation.class);

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

    Pageable page = new PageRequest(0, 10);
    when(orderableService.searchOrderables(any(QueryOrderableSearchParams.class), eq(page)))
        .thenReturn(Pagination.getPage(items, page));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam("page", page.getPageNumber())
        .queryParam("size", page.getPageSize())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
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

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowPaginationWithZeroSize() {
    final List<Orderable> items = Collections.singletonList(orderable);

    Pageable page = new PageRequest(0, 0);
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

    Pageable page = new PageRequest(0, 0);
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
        .extract().as(OrderableDto.class);

    assertEquals(orderableId, response.getId());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindOrderableByIdentityIdAndVersionId() {
    when(orderableRepository.findByIdentityIdAndIdentityVersionId(
        orderable.getId(), orderableVersionId)).thenReturn(orderable);

    OrderableDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, orderableId)
        .queryParam(VERSION_ID, orderableVersionId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDto.class);

    assertEquals(orderableId, response.getId());
    assertEquals(orderableVersionId, orderable.getVersionId());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // POST /orderables/search

  @Test
  public void shouldPostSearchOrderables() {
    OrderableSearchParams searchParams = new OrderableSearchParams(
        orderableDto.getProductCode(), orderableDto.getFullProductName(),
        orderable.getProductCode().toString(),
        Lists.newArrayList(new VersionIdentityDto(
            orderableDto.getId(), orderableDto.getVersionId())),
        0, 10);

    given(orderableRepository
        .search(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(orderable)));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .body(searchParams)
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(orderableDto.getId().toString(),
        ((java.util.LinkedHashMap) response.getContent().get(0)).get("id"));
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
}

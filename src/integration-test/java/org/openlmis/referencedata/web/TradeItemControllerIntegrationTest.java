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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;
import static org.openlmis.referencedata.dto.TradeItemDto.newInstance;
import static org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys.ERROR_MANUFACTURER_REQUIRED;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.TradeItemDto;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class TradeItemControllerIntegrationTest extends AuditLogWebIntegrationTest<TradeItem> {

  private static final String RESOURCE_URL = "/api/tradeItems";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_URL = ID_URL + "/auditLog";
  private static final String CID = "cid";

  @Before
  public void setUp() {
    when(tradeItemRepository.save(any(TradeItem.class))).thenAnswer(new SaveAnswer<TradeItem>());
  }

  @Test
  public void shouldCreateNewTradeItem() {
    mockUserHasRight(ORDERABLES_MANAGE);

    TradeItem tradeItem = generateItem("item");

    when(tradeItemRepository.save(any(TradeItem.class))).thenAnswer(new SaveAnswer<TradeItem>());

    TradeItemDto object = newInstance(tradeItem);
    TradeItemDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(object)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TradeItemDto.class);

    assertEquals(object, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateIfManufacturerOfTradeItemIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);

    TradeItemDto object = newInstance(new TradeItem("", Collections.emptyList()));

    checkBadRequestBody(object, ERROR_MANUFACTURER_REQUIRED, RESOURCE_URL);
  }

  @Test
  public void shouldRetrieveAllTradeItems() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(tradeItemRepository.findAll()).thenReturn(items);

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<TradeItemDto> expected = newInstance(items);
    checkIfEquals(response, expected);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveTradeItemsByPartialMatch() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(tradeItemRepository.findByClassificationIdLike(CID)).thenReturn(items);

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("classificationId", CID)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<TradeItemDto> expected = newInstance(items);
    checkIfEquals(response, expected);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveTradeItemsByFullMatch() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(tradeItemRepository.findByClassificationId(CID)).thenReturn(items);

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("classificationId", CID)
        .queryParam("fullMatch", "true")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<TradeItemDto> expected = newInstance(items);
    checkIfEquals(response, expected);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDenyAccessToUpdate() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(generateItem("name"))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);
  }

  @Test
  public void shouldDenyAccessToRetrieve() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);
  }

  @Override
  protected void mockHasNoAuditRight() {
    mockUserHasNoRight(RightName.ORDERABLES_MANAGE);
  }

  @Override
  protected void mockHasAuditRight() {
    mockUserHasRight(RightName.ORDERABLES_MANAGE);
  }

  @Override
  protected TradeItem getInstance() {
    return generateItem("abc");
  }

  @Override
  protected CrudRepository<TradeItem, UUID> getRepository() {
    return tradeItemRepository;
  }

  @Override
  protected String getAuditAddress() {
    return AUDIT_URL;
  }

  @Override
  protected String getErrorNotFoundMessage() {
    return TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID;
  }

  private TradeItem generateItem(String manufacturer) {
    TradeItem tradeItem = new TradeItem(manufacturer, new ArrayList<>());
    tradeItem.assignCommodityType("sys1", "sys1Id");
    tradeItem.assignCommodityType("sys2", "sys2Id");
    return tradeItem;
  }

  private void checkIfEquals(PageImplRepresentation response, List<TradeItemDto> expected) {
    List pageContent = response.getContent();
    assertEquals(expected.size(), pageContent.size());
    for (int i = 0; i < pageContent.size(); i++) {
      Map<String, String> retrieved = (LinkedHashMap) pageContent.get(i);
      assertEquals(expected.get(i).getManufacturerOfTradeItem(),
          retrieved.get("manufacturerOfTradeItem"));
    }
  }
}

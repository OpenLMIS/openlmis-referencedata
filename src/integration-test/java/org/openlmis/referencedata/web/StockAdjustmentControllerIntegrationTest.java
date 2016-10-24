package org.openlmis.referencedata.web;

import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.openlmis.referencedata.repository.StockAdjustmentReasonRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

public class StockAdjustmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/stockAdjustmentReasons";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  @MockBean
  private StockAdjustmentReasonRepository stockAdjustmentReasonRepository;

  private StockAdjustmentReason stockAdjustmentReason;
  private UUID stockAdjustmentReasonId;

  /**
   * Constructor for tests.
   */
  public StockAdjustmentControllerIntegrationTest() {
    stockAdjustmentReason = new StockAdjustmentReason();
    stockAdjustmentReason.setName("StockAdjustmentReason");
    stockAdjustmentReasonId = UUID.randomUUID();
    Program program = new Program("code");
    program.setId(UUID.randomUUID());
    stockAdjustmentReason.setProgram(program);
  }

  @Test
  public void shouldDeleteStockAdjustmentReason() {

    given(stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId))
            .willReturn(stockAdjustmentReason);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", stockAdjustmentReasonId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);
  }

  @Test
  public void shouldPostStockAdjustmentReason() {

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(stockAdjustmentReason)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(StockAdjustmentReason.class);

    assertEquals(stockAdjustmentReason.getId(), response.getId());
  }

  @Test
  public void shouldPutStockAdjustmentReason() {

    stockAdjustmentReason.setDescription("description");
    given(stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId))
            .willReturn(stockAdjustmentReason);

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", stockAdjustmentReasonId)
        .body(stockAdjustmentReason)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason.class);

    assertEquals(stockAdjustmentReason.getId(), response.getId());
    assertEquals("description", response.getDescription());
  }

  @Test
  public void shouldGetAllStockAdjustmentReasons() {

    List<StockAdjustmentReason> storedStockAdjustmentReasons = Arrays.asList(
            stockAdjustmentReason, new StockAdjustmentReason());
    given(stockAdjustmentReasonRepository.findAll()).willReturn(storedStockAdjustmentReasons);

    StockAdjustmentReason[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason[].class);

    assertEquals(storedStockAdjustmentReasons.size(), response.length);
  }

  @Test
  public void shouldGetStockAdjustmentReason() {

    given(stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId))
            .willReturn(stockAdjustmentReason);

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", stockAdjustmentReasonId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason.class);

    assertEquals(stockAdjustmentReason.getId(), response.getId());
  }

  @Test
  public void shouldFindStockAdjustmentReasonsByProgram() {
    UUID programId = stockAdjustmentReason.getProgram().getId();
    List<StockAdjustmentReason> listToReturn = new ArrayList<>();
    listToReturn.add(stockAdjustmentReason);
    given(stockAdjustmentReasonRepository.findByProgramId(programId))
        .willReturn(listToReturn);
    StockAdjustmentReason[] response = restAssured.given()
        .queryParam("program", programId)
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason[].class);

    List<StockAdjustmentReason> foundStockAdjustmentReason = Arrays.asList(response);
    assertEquals(1, foundStockAdjustmentReason.size());
    assertEquals("StockAdjustmentReason", foundStockAdjustmentReason.get(0).getName());
  }
}

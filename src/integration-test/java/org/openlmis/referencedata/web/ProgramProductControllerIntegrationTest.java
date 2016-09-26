package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.openlmis.referencedata.repository.ProgramProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Ignore
@SuppressWarnings("PMD.TooManyMethods")
public class ProgramProductControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/programProducts";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String PROGRAM = "program";

  @Autowired
  private ProgramProductRepository programProductRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableProductRepository orderableProductRepository;

  private List<ProgramProduct> programProducts;

  private Integer currentInstanceNumber;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
    programProducts = new ArrayList<>();
    for ( int programProductNumber = 0; programProductNumber < 5; programProductNumber++ ) {
      programProducts.add(generateProgramProduct());
    }
  }

  @Test
  public void shouldFindProgramProducts() {
    ProgramProduct[] response = restAssured.given()
            .queryParam(PROGRAM, programProducts.get(0).getProgram().getId())
            .queryParam(ACCESS_TOKEN, getToken())
            .when()
            .get(SEARCH_URL)
            .then()
            .statusCode(200)
            .extract().as(ProgramProduct[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for ( ProgramProduct programProduct : response ) {
      assertEquals(
              programProduct.getProgram().getId(),
              programProducts.get(0).getProgram().getId());
    }
  }

  @Test
  public void shouldDeleteProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(programProductRepository.exists(programProduct.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);
    programProductRepository.delete(programProduct);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(programProduct)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .body(programProduct)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProgramProducts() {

    ProgramProduct[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct[].class);

    Iterable<ProgramProduct> programProducts = Arrays.asList(response);
    assertTrue(programProducts.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);

    ProgramProduct response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct.class);

    assertTrue(programProductRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProgramProduct generateProgramProduct() {
    OrderableProduct orderableProduct = GlobalProduct.newGlobalProduct("abcd", "Abcd", "test", 10);
    orderableProductRepository.save(orderableProduct);
    Program program = generateProgram();
    ProductCategory testCategory = ProductCategory.createNew(Code.code("testCat"));
    ProgramProduct programProduct = ProgramProduct.createNew(program,
        testCategory,
        orderableProduct,
        10,
        true,
        true,
        0,
        1);
    orderableProduct.addToProgram(programProduct);
    orderableProductRepository.save(orderableProduct);
    return programProduct;
  }

  private Program generateProgram() {
    Program program = new Program("code" + generateInstanceNumber());
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}

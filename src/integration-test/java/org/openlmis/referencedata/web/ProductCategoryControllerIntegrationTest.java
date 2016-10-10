package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProductCategoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/productCategories";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String CODE = "code";
  private static final String ACCESS_TOKEN = "access_token";

  @MockBean
  private ProductCategoryRepository productCategoryRepository;

  private Integer currentInstanceNumber;

  private ProductCategory productCategory;
  private UUID productCategoryId;

  /**
   * Constructor for tests.
   */
  public ProductCategoryControllerIntegrationTest() {
    currentInstanceNumber = 0;
    productCategory = generateProductCategory();
    productCategoryId = UUID.randomUUID();
  }

  @Test
  public void shouldFindProductCategoriesByCode() {

    given(productCategoryRepository.findByCode(any(Code.class))).willReturn(productCategory);

    ProductCategory response = restAssured
        .given()
        .queryParam(CODE, productCategory.getCode())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory.class);

    assertEquals(productCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindAllProductCategories() {

    Iterable<ProductCategory> searchResult = Collections.singletonList(productCategory);
    given(productCategoryRepository.findAll()).willReturn(searchResult);

    ProductCategory[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory[].class);

    assertEquals(1, response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProductCategory generateProductCategory() {
    Integer instanceNumber = generateInstanceNumber();
    ProductCategory productCategory = ProductCategory.createNew(
        Code.code("productCategoryCode" + instanceNumber),
        new OrderedDisplayValue("productCategoryName" + instanceNumber, instanceNumber));
    return productCategory;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  @Test
  public void shouldDeleteProductCategory() {

    given(productCategoryRepository.findOne(productCategoryId)).willReturn(productCategory);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", productCategoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutProductCategory() {

    ProductCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(productCategory)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory.class);

    assertEquals(productCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutWithIdProductCategory() {

    given(productCategoryRepository.findOne(productCategoryId)).willReturn(productCategory);

    ProductCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", productCategoryId)
        .body(productCategory)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory.class);

    assertEquals(productCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProductCategories() {

    List<ProductCategory> storedProductCategories = Arrays.asList(productCategory,
        generateProductCategory());
    given(productCategoryRepository.findAll()).willReturn(storedProductCategories);

    ProductCategory[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory[].class);

    assertEquals(storedProductCategories.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProductCategory() {

    given(productCategoryRepository.findOne(productCategoryId)).willReturn(productCategory);

    ProductCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", productCategoryId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProductCategory.class);

    assertEquals(productCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}

package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.joda.money.CurrencyUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

@Ignore
public class FacilityTypeApprovedProductControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/facilityTypeApprovedProducts";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private FacilityTypeApprovedProductRepository repository;

  @Value("${currencyCode}")
  private String currencyCode;

  private Program program;
  private Orderable orderable;
  private FacilityType facilityType1;
  private ProgramOrderable programOrderable;
  private FacilityTypeApprovedProduct facilityTypeAppProd;
  private UUID facilityTypeAppProdId;

  /**
   * Constructor for tests.
   */
  public FacilityTypeApprovedProductControllerIntegrationTest() {

    program = new Program("programCode");
    program.setPeriodsSkippable(true);
    program.setId(UUID.randomUUID());

    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode"),
        new OrderedDisplayValue("orderableDisplayCategoryName", 1));
    orderableDisplayCategory.setId(UUID.randomUUID());

    orderable = CommodityType.newCommodityType("abcd", "each", "Abcd", "test", 10, 5, false);
    orderable.setId(UUID.randomUUID());

    programOrderable = ProgramOrderable.createNew(program, orderableDisplayCategory, orderable,
        CurrencyUnit.of(currencyCode));

    facilityType1 = new FacilityType("facilityType1");

    facilityTypeAppProd = new FacilityTypeApprovedProduct();
    facilityTypeAppProd.setId(facilityTypeAppProdId);
    facilityTypeAppProd.setFacilityType(facilityType1);
    facilityTypeAppProd.setProgramOrderable(programOrderable);
    facilityTypeAppProd.setMaxMonthsOfStock(6.00);
    facilityTypeAppProdId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteFacilityTypeApprovedProduct() {

    given(repository.findOne(facilityTypeAppProdId)).willReturn(facilityTypeAppProd);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostFacilityTypeApprovedProduct() {

    FacilityTypeApprovedProduct response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityTypeAppProd)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(FacilityTypeApprovedProduct.class);

    assertEquals(facilityTypeAppProd, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutFacilityTypeApprovedProduct() {

    facilityTypeAppProd.setMaxMonthsOfStock(9.00);
    given(repository.findOne(facilityTypeAppProdId)).willReturn(facilityTypeAppProd);

    FacilityTypeApprovedProduct response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .body(facilityTypeAppProd)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityTypeApprovedProduct.class);

    assertEquals(facilityTypeAppProd, response);
    assertEquals(9.00, response.getMaxMonthsOfStock(), 0.00);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetFacilityTypeApprovedProduct() {

    given(repository.findOne(facilityTypeAppProdId)).willReturn(facilityTypeAppProd);

    FacilityTypeApprovedProduct response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeAppProdId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityTypeApprovedProduct.class);

    assertEquals(facilityTypeAppProd, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}

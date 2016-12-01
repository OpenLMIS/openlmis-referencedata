package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public class ProductCategoryControllerTest {

  @Mock
  private ProductCategoryRepository repository;

  private ProductCategoryController controller;

  private ProductCategory categoryA;
  private Code codeA;
  private OrderedDisplayValue displayA;
  private ProductCategory categoryB;
  private Code codeB;
  private OrderedDisplayValue displayB;

  /**
   * Constructor for test.
   */
  public ProductCategoryControllerTest() {
    initMocks(this);
    controller = new ProductCategoryController(repository);

    codeA = Code.code("A");
    displayA = new OrderedDisplayValue("A-Analgesics", 1);
    categoryA = ProductCategory.createNew(codeA, displayA);
    codeB = Code.code("B");
    displayB = new OrderedDisplayValue("B-Bandages", 2);
    categoryB = ProductCategory.createNew(codeB, displayB);
  }

  @Before
  public void setup() {
  }

  private void preparePostOrPut() {
    when(repository.findAll()).thenReturn(
        Sets.newHashSet(new ProductCategory[]{categoryA, categoryB})
    );
  }

  @Test
  public void shouldGetAllProductCategories() {
    //given
    Set<ProductCategory> expected = Sets.newHashSet(new ProductCategory[]{categoryA, categoryB});
    preparePostOrPut();

    //when
    ResponseEntity responseEntity = controller.getAllProductCategories();
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<ProductCategory> categories = (Set<ProductCategory>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expected, categories);
  }
}

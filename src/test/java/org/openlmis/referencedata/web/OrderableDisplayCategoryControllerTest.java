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
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public class OrderableDisplayCategoryControllerTest {

  @Mock
  private OrderableDisplayCategoryRepository repository;

  private OrderableDisplayCategoryController controller;

  private OrderableDisplayCategory categoryA;
  private Code codeA;
  private OrderedDisplayValue displayA;
  private OrderableDisplayCategory categoryB;
  private Code codeB;
  private OrderedDisplayValue displayB;

  /**
   * Constructor for test.
   */
  public OrderableDisplayCategoryControllerTest() {
    initMocks(this);
    controller = new OrderableDisplayCategoryController(repository);

    codeA = Code.code("A");
    displayA = new OrderedDisplayValue("A-Analgesics", 1);
    categoryA = OrderableDisplayCategory.createNew(codeA, displayA);
    codeB = Code.code("B");
    displayB = new OrderedDisplayValue("B-Bandages", 2);
    categoryB = OrderableDisplayCategory.createNew(codeB, displayB);
  }

  @Before
  public void setup() {
  }

  private void preparePostOrPut() {
    when(repository.findAll()).thenReturn(
        Sets.newHashSet(new OrderableDisplayCategory[]{categoryA, categoryB})
    );
  }

  @Test
  public void shouldGetAllOrderableDisplayCategories() {
    //given
    Set<OrderableDisplayCategory> expected = Sets.newHashSet(
        new OrderableDisplayCategory[]{categoryA, categoryB});
    preparePostOrPut();

    //when
    ResponseEntity responseEntity = controller.getAllOrderableDisplayCategories();
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<OrderableDisplayCategory> categories =
        (Set<OrderableDisplayCategory>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expected, categories);
  }
}

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
    Iterable<OrderableDisplayCategory> categories =
        controller.getAllOrderableDisplayCategories();

    //then
    assertEquals(expected, categories);
  }
}

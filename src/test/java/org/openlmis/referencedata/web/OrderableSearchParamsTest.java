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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

public class OrderableSearchParamsTest {

  private static final String VALUE = "test";

  @Test
  public void getCodeShouldReturnValueForKeyCode() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("code", VALUE);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals(VALUE, orderableSearchParams.getCode());
  }

  @Test
  public void getCodeShouldReturnNullIfMapDoesNotContainKeyCode() {
    OrderableSearchParams orderableSearchParams =
        new OrderableSearchParams(new LinkedMultiValueMap<>());

    assertNull(orderableSearchParams.getCode());
  }

  @Test
  public void getCodeShouldReturnEmptyStringIfValueForRequestParamIsNull() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("code", null);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals("", orderableSearchParams.getCode());
  }

  @Test
  public void getNameShouldReturnValueForKeyName() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("name", VALUE);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals(VALUE, orderableSearchParams.getName());
  }

  @Test
  public void getNameShouldReturnNullIfMapDoesNotContainKeyName() {
    OrderableSearchParams orderableSearchParams =
        new OrderableSearchParams(new LinkedMultiValueMap<>());

    assertNull(orderableSearchParams.getName());
  }

  @Test
  public void getNameShouldReturnEmptyStringIfValueForRequestParamIsNull() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("name", null);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals("", orderableSearchParams.getName());
  }

  @Test
  public void getProgramCodeShouldReturnValueForKeyProgram() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("program", VALUE);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals(VALUE, orderableSearchParams.getProgramCode().toString());
  }

  @Test
  public void getProgramShouldReturnNullIfMapDoesNotContainKeyProgram() {
    OrderableSearchParams orderableSearchParams =
        new OrderableSearchParams(new LinkedMultiValueMap<>());

    assertNull(orderableSearchParams.getProgramCode());
  }

  @Test
  public void getProgramShouldReturnEmptyStringIfValueForRequestParamIsNull() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("program", null);
    OrderableSearchParams orderableSearchParams = new OrderableSearchParams(queryMap);

    assertEquals("", orderableSearchParams.getProgramCode().toString());
  }

}
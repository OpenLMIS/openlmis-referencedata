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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

public class QueryOrderableSearchParamsTest {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM = "program";

  private static final String VALUE = "test";
  private static final String ANOTHER_VALUE = "anotherTest";

  @Test
  public void getCodeShouldReturnValueForKeyCode() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(CODE, VALUE);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    assertEquals(VALUE, searchParams.getCode());
  }

  @Test
  public void getCodeShouldReturnNullIfMapDoesNotContainKeyCode() {
    QueryOrderableSearchParams searchParams =
        new QueryOrderableSearchParams(new LinkedMultiValueMap<>());

    assertNull(searchParams.getCode());
  }

  @Test
  public void getCodeShouldReturnEmptyStringIfValueForRequestParamIsNull() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(CODE, null);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    assertEquals("", searchParams.getCode());
  }

  @Test
  public void getNameShouldReturnValueForKeyName() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(NAME, VALUE);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    assertEquals(VALUE, searchParams.getName());
  }

  @Test
  public void getNameShouldReturnNullIfMapDoesNotContainKeyName() {
    QueryOrderableSearchParams searchParams =
        new QueryOrderableSearchParams(new LinkedMultiValueMap<>());

    assertNull(searchParams.getName());
  }

  @Test
  public void getNameShouldReturnEmptyStringIfValueForRequestParameterIsNull() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(NAME, null);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    assertEquals("", searchParams.getName());
  }


  @Test
  public void getProgramCodesShouldReturnValueForKeyProgram() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM, VALUE);
    queryMap.add(PROGRAM, ANOTHER_VALUE);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    Set<String> programCodes = new HashSet<>();
    programCodes.add(VALUE);
    programCodes.add(ANOTHER_VALUE);

    assertEquals(programCodes, searchParams.getProgramCodes());
  }

  @Test
  public void getProgramCodesShouldReturnEmptyCollectionIfMapDoesNotContainKeyProgram() {
    QueryOrderableSearchParams searchParams =
        new QueryOrderableSearchParams(new LinkedMultiValueMap<>());

    MatcherAssert.assertThat(searchParams.getProgramCodes(), hasSize(0));
  }

  @Test
  public void getProgramCodesShouldReturnOnlyNotBlankProgramCodeRequestParameters() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM, null);
    queryMap.add(PROGRAM, "");
    queryMap.add(PROGRAM, " ");
    queryMap.add(PROGRAM, VALUE);
    queryMap.add(PROGRAM, ANOTHER_VALUE);
    QueryOrderableSearchParams searchParams = new QueryOrderableSearchParams(queryMap);

    MatcherAssert.assertThat(searchParams.getProgramCodes(), hasItems(VALUE, ANOTHER_VALUE));
  }

}

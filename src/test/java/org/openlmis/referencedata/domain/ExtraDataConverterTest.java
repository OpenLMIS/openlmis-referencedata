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

package org.openlmis.referencedata.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ExtraDataConverterTest {

  private ObjectMapper objectMapper;
  private ExtraDataConverter converter;

  private Map<String, String> extraData;
  private String extraDataAsString;

  private String expectedDatabaseValue;
  private Map<String, String> expectedEntityAttribute;

  /**
   * Constructor for parameterized test.
   */
  public ExtraDataConverterTest(Map<String, String> extraData, String extraDataAsString,
      String expectedDatabaseValue, Map<String, String> expectedEntityAttribute,
      Consumer<ObjectMapper> callback) {
    this.extraData = extraData;
    this.extraDataAsString = extraDataAsString;

    this.expectedDatabaseValue = expectedDatabaseValue;
    this.expectedEntityAttribute = expectedEntityAttribute;

    this.objectMapper = spy(ObjectMapper.class);
    this.converter = new ExtraDataConverter(objectMapper);

    Optional
        .ofNullable(callback)
        .ifPresent(fun -> fun.accept(objectMapper));
  }

  /**
   * Data for tests.
   */
  @Parameters(name = "extraData = {0}, extraDataAsString = {1},"
      + "expectedDatabaseValue = {2}, expectedEntityAttribute = {3}")
  public static Collection<Object[]> data() {
    List<Object[]> data = Lists.newArrayList();

    // converter should handle null values
    data.add(new Object[]{
        null, null,
        null, Maps.newHashMap(),
        null});

    // converter should handle empty values
    data.add(new Object[]{
        Maps.newHashMap(), "",
        null, Maps.newHashMap(),
        null});
    data.add(new Object[]{
        Maps.newHashMap(), "     ",
        null, Maps.newHashMap(),
        null});

    // converter should handle normal values
    data.add(new Object[]{
        ImmutableMap.of("a", "b"), "{\"c\":\"d\"}",
        "{\"a\":\"b\"}", ImmutableMap.of("c", "d"),
        null});

    // converer should return default values if exception occurs
    Consumer<ObjectMapper> callback = ExtraDataConverterTest::doThrowException;
    data.add(new Object[]{
        ImmutableMap.of("e", "f"), "{\"g\":\"h\"}",
        null, Maps.newHashMap(),
        callback});

    return data;
  }

  @Test
  public void shouldConvertToDatabaseColumn() {
    assertThat(converter.convertToDatabaseColumn(extraData))
        .isEqualTo(expectedDatabaseValue);
  }

  @Test
  public void shouldConvertToEntityAttribute() {
    assertThat(converter.convertToEntityAttribute(extraDataAsString))
        .isEqualTo(expectedEntityAttribute);
  }

  private static void doThrowException(ObjectMapper mapper) {
    try {
      doThrow(mock(JsonProcessingException.class))
          .when(mapper)
          .writeValueAsString(anyMapOf(String.class, String.class));

      doThrow(new IOException())
          .when(mapper)
          .readValue(anyString(), any(TypeReference.class));
    } catch (IOException exp) {
      throw new IllegalStateException(exp);
    }
  }
}

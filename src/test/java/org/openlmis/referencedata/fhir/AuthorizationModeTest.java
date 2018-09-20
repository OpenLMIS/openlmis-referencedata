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

package org.openlmis.referencedata.fhir;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Locale;
import org.junit.Test;

public class AuthorizationModeTest {

  @Test
  public void shouldConvertStringToEnumValue() {
    for (AuthorizationMode value : AuthorizationMode.values()) {
      assertThat(AuthorizationMode.fromString(value.toString())).isEqualTo(value);
    }
  }

  @Test
  public void shouldConvertStringToEnumValueCaseIgnore() {
    for (AuthorizationMode value : AuthorizationMode.values()) {
      String modeAsString = value.toString();
      String modeAsStringUpper = modeAsString.toUpperCase(Locale.ENGLISH);
      String modeAsStringLower = modeAsString.toLowerCase(Locale.ENGLISH);
      String modeAsStringMixed = mixStringCase(modeAsString);

      assertThat(AuthorizationMode.fromString(modeAsStringUpper)).isEqualTo(value);
      assertThat(AuthorizationMode.fromString(modeAsStringLower)).isEqualTo(value);
      assertThat(AuthorizationMode.fromString(modeAsStringMixed)).isEqualTo(value);
    }
  }

  @Test
  public void shouldReturnNoneWhenParameterIsNull() {
    assertThat(AuthorizationMode.fromString(null)).isEqualTo(AuthorizationMode.NONE);
  }

  @Test
  public void shouldReturnNoneWhenParameterIsBlank() {
    assertThat(AuthorizationMode.fromString("")).isEqualTo(AuthorizationMode.NONE);
    assertThat(AuthorizationMode.fromString("       ")).isEqualTo(AuthorizationMode.NONE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfParameterIsUnknown() {
    AuthorizationMode.fromString("unknown-mode");
  }

  private String mixStringCase(String input) {
    char[] chars = input.toCharArray();

    for (int i = 1; i < chars.length; i += 2) {
      chars[i] = Character.toLowerCase(chars[i]);
    }

    return new String(chars);
  }

}

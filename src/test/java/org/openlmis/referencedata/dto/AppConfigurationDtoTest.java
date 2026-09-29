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

package org.openlmis.referencedata.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.ToStringTestUtils;

public class AppConfigurationDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(AppConfigurationDto.class)
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        .verify();
    EqualsVerifier.forClass(AppConfigurationDto.ThemeDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
    EqualsVerifier.forClass(AppConfigurationDto.LogoDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(AppConfigurationDto.class, new AppConfigurationDto());
    ToStringTestUtils.verify(AppConfigurationDto.ThemeDto.class,
        new AppConfigurationDto.ThemeDto("teal", "dark"));
    ToStringTestUtils.verify(AppConfigurationDto.LogoDto.class,
        new AppConfigurationDto.LogoDto("/logo", "image/png", 1));
  }

  @Test
  public void shouldReadThemeFieldsThroughImporter() {
    AppConfigurationDto dto = new AppConfigurationDto();
    assertNull(dto.getThemePreset());
    assertNull(dto.getDefaultAppearance());

    dto.setTheme(new AppConfigurationDto.ThemeDto("teal", "dark"));

    assertEquals("teal", dto.getThemePreset());
    assertEquals("dark", dto.getDefaultAppearance());
  }

  @Test
  public void shouldAcceptLogoAsSent() {
    AppConfigurationDto dto = new AppConfigurationDto();
    AppConfigurationDto.LogoDto logo = new AppConfigurationDto.LogoDto("/logo", "image/png", 1);

    dto.setLogo(logo);

    assertEquals(logo, dto.getLogo());
  }
}

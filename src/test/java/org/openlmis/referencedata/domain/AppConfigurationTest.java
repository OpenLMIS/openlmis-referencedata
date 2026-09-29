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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.dto.AppConfigurationDto;

public class AppConfigurationTest {
  private static final String SHA = "abc";
  private static final String PNG = "image/png";

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(AppConfiguration.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldCreateDefaultWithSingletonIdAndNoSettings() {
    AppConfiguration configuration = AppConfiguration.newDefault();

    assertEquals(AppConfiguration.SINGLETON_ID, configuration.getId());
    assertEquals(Long.valueOf(0), configuration.getVersion());
    assertNull(configuration.getAppName());
    assertTrue(configuration.getFeatureFlags().isEmpty());
    assertNotNull(configuration.getModifiedDate());
  }

  @Test
  public void shouldUpdateFromImporterTrimmingTheName() {
    AppConfigurationDto importer = new AppConfigurationDto();
    importer.setAppName("  SIGECA  ");
    importer.setTheme("teal", "dark");
    importer.setFeatureFlags(Collections.singletonMap("GS1_SCANNING", true));
    AppConfiguration configuration = AppConfiguration.newDefault();

    configuration.updateFrom(importer);

    assertEquals("SIGECA", configuration.getAppName());
    assertEquals("teal", configuration.getThemePreset());
    assertEquals("dark", configuration.getDefaultAppearance());
    assertEquals(true, configuration.getFeatureFlags().get("GS1_SCANNING"));
  }

  @Test
  public void shouldStoreNullForBlankNameAndEmptyFlagsForNone() {
    AppConfiguration configuration = AppConfiguration.newDefault();
    configuration.setAppName("Old");
    AppConfigurationDto importer = new AppConfigurationDto();
    importer.setAppName("   ");

    configuration.updateFrom(importer);

    assertNull(configuration.getAppName());
    assertNull(configuration.getThemePreset());
    assertTrue(configuration.getFeatureFlags().isEmpty());
  }

  @Test
  public void shouldBumpVersionAndDateWhenModified() {
    AppConfiguration configuration = AppConfiguration.newDefault();

    configuration.markModified();

    assertEquals(Long.valueOf(1), configuration.getVersion());
    assertTrue(configuration.isAtVersion(1));
    assertFalse(configuration.isAtVersion(0));
  }

  @Test
  public void shouldStartVersionAtOneWhenNoneIsSet() {
    AppConfiguration configuration = new AppConfiguration();
    configuration.setVersion(null);

    assertFalse(configuration.isAtVersion(0));
    configuration.markModified();

    assertEquals(Long.valueOf(1), configuration.getVersion());
  }

  @Test
  public void shouldSetAndClearLogo() {
    AppConfiguration configuration = AppConfiguration.newDefault();

    configuration.setLogo(SHA, PNG, 10);
    assertTrue(configuration.hasLogo());

    configuration.clearLogo();
    assertFalse(configuration.hasLogo());
    assertNull(configuration.getLogoContentType());
    assertNull(configuration.getLogoSize());
  }

  @Test
  public void shouldExportLogoOnlyWhenSet() {
    AppConfiguration configuration = AppConfiguration.newDefault();
    configuration.setFeatureFlags(null);
    AppConfigurationDto withoutLogo = new AppConfigurationDto();

    configuration.export(withoutLogo);

    assertNull(withoutLogo.getLogo());
    assertTrue(withoutLogo.getFeatureFlags().isEmpty());

    configuration.setLogo(SHA, PNG, 10);
    AppConfigurationDto withLogo = new AppConfigurationDto();
    configuration.export(withLogo);

    assertEquals(AppConfigurationDto.LOGO_PATH + "?v=" + SHA, withLogo.getLogo().getUrl());
    assertEquals(PNG, withLogo.getLogo().getContentType());
    assertEquals(Integer.valueOf(10), withLogo.getLogo().getSize());
  }

  @Test
  public void shouldExportFlagsAsStored() {
    AppConfiguration configuration = AppConfiguration.newDefault();
    Map<String, Object> flags = Collections.singletonMap("QUANTITY_UNIT_OPTION", "BOTH");
    configuration.setFeatureFlags(flags);
    AppConfigurationDto dto = new AppConfigurationDto();

    configuration.export(dto);

    assertEquals(flags, dto.getFeatureFlags());
  }
}

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.AppConfiguration;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AppConfigurationDto
    implements AppConfiguration.Exporter, AppConfiguration.Importer {
  public static final String LOGO_PATH = "/api/appConfiguration/logo";

  private Long version;
  private String appName;
  private Boolean showAppName;
  private LogoDto logo;
  private ThemeDto theme;
  private Map<String, Object> featureFlags;
  private ZonedDateTime modifiedDate;

  @Override
  @JsonIgnore
  public String getThemePreset() {
    return theme == null ? null : theme.getPreset();
  }

  @Override
  @JsonIgnore
  public String getDefaultAppearance() {
    return theme == null ? null : theme.getDefaultAppearance();
  }

  // Lombok generates no setter when a method of that name exists, so both are written out.
  public void setTheme(ThemeDto theme) {
    this.theme = theme;
  }

  @Override
  public void setTheme(String preset, String defaultAppearance) {
    theme = new ThemeDto(preset, defaultAppearance);
  }

  public void setLogo(LogoDto logo) {
    this.logo = logo;
  }

  @Override
  public void setLogo(String sha256, String contentType, Integer size) {
    logo = new LogoDto(LOGO_PATH + "?v=" + sha256, contentType, size);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  public static final class ThemeDto {
    private String preset;
    private String defaultAppearance;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  public static final class LogoDto {
    private String url;
    private String contentType;
    private Integer size;
  }
}

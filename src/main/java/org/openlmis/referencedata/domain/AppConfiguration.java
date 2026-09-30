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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "app_configuration")
@TypeName("AppConfiguration")
public class AppConfiguration extends BaseEntity {
  public static final UUID SINGLETON_ID = UUID.fromString("5c3d6b1e-0000-4000-8000-000000000001");
  public static final int MAX_LOGO_SIZE = 512 * 1024;

  private String appName;

  @Column(nullable = false)
  private boolean showAppName = true;

  private String themePreset;

  private String defaultAppearance;

  @Convert(converter = ExtraDataConverter.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> featureFlags = new HashMap<>();

  private String logoSha256;

  private String logoContentType;

  private Integer logoSize;

  @Column(nullable = false)
  private Long version = 0L;

  @Column(nullable = false)
  private ZonedDateTime modifiedDate;

  /** The configuration used when nothing is stored: every setting at its default. */
  public static AppConfiguration newDefault() {
    AppConfiguration configuration = new AppConfiguration();
    configuration.setId(SINGLETON_ID);
    configuration.setModifiedDate(ZonedDateTime.now(ZoneOffset.UTC));
    return configuration;
  }

  /** Replaces the editable settings; the logo changes separately. */
  public void updateFrom(Importer importer) {
    appName = StringUtils.trimToNull(importer.getAppName());
    showAppName = !Boolean.FALSE.equals(importer.getShowAppName());
    themePreset = importer.getThemePreset();
    defaultAppearance = importer.getDefaultAppearance();
    featureFlags = importer.getFeatureFlags() == null
        ? new HashMap<>()
        : new HashMap<>(importer.getFeatureFlags());
  }

  /** Points the configuration at a stored logo. */
  public void setLogo(String sha256, String contentType, int size) {
    logoSha256 = sha256;
    logoContentType = contentType;
    logoSize = size;
  }

  /** Drops the logo, so the UI shows its built-in one. */
  public void clearLogo() {
    logoSha256 = null;
    logoContentType = null;
    logoSize = null;
  }

  public boolean hasLogo() {
    return logoSha256 != null;
  }

  public boolean isAtVersion(long expectedVersion) {
    return version != null && version == expectedVersion;
  }

  /** Bumps the version and the modification date. */
  public void markModified() {
    version = version == null ? 1L : version + 1;
    modifiedDate = ZonedDateTime.now(ZoneOffset.UTC);
  }

  /** Exports the current state. */
  public void export(Exporter exporter) {
    exporter.setVersion(version);
    exporter.setAppName(appName);
    exporter.setShowAppName(showAppName);
    exporter.setTheme(themePreset, defaultAppearance);
    exporter.setFeatureFlags(featureFlags == null ? new HashMap<>() : featureFlags);
    if (hasLogo()) {
      exporter.setLogo(logoSha256, logoContentType, logoSize);
    }
    exporter.setModifiedDate(modifiedDate);
  }

  public interface Exporter {
    void setVersion(Long version);

    void setAppName(String appName);

    void setShowAppName(Boolean showAppName);

    void setTheme(String preset, String defaultAppearance);

    void setFeatureFlags(Map<String, Object> featureFlags);

    void setLogo(String sha256, String contentType, Integer size);

    void setModifiedDate(ZonedDateTime modifiedDate);
  }

  public interface Importer {
    String getAppName();

    Boolean getShowAppName();

    String getThemePreset();

    String getDefaultAppearance();

    Map<String, Object> getFeatureFlags();
  }
}

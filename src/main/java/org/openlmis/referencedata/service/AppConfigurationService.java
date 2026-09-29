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

package org.openlmis.referencedata.service;

import static org.openlmis.referencedata.domain.AppConfiguration.SINGLETON_ID;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.openlmis.referencedata.domain.AppConfiguration;
import org.openlmis.referencedata.domain.AppConfigurationLogo;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.exception.VersionMismatchException;
import org.openlmis.referencedata.repository.AppConfigurationLogoRepository;
import org.openlmis.referencedata.repository.AppConfigurationRepository;
import org.openlmis.referencedata.util.ImageTypeDetector;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.AppConfigurationMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppConfigurationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationService.class);

  @Autowired
  private AppConfigurationRepository appConfigurationRepository;

  @Autowired
  private AppConfigurationLogoRepository appConfigurationLogoRepository;

  /** Returns the stored configuration, or the all-defaults one. */
  public AppConfiguration get() {
    return appConfigurationRepository.findById(SINGLETON_ID)
        .orElseGet(AppConfiguration::newDefault);
  }

  /** Returns the logo the configuration points at, if any. */
  public Optional<AppConfigurationLogo> getLogo() {
    AppConfiguration configuration = get();
    if (!configuration.hasLogo()) {
      return Optional.empty();
    }
    return appConfigurationLogoRepository.findById(configuration.getLogoSha256());
  }

  /** Replaces the name, theme and feature flags. */
  @Transactional
  public AppConfiguration update(long expectedVersion, AppConfiguration.Importer importer) {
    AppConfiguration configuration = lockForChange(expectedVersion);
    configuration.updateFrom(importer);
    configuration.markModified();
    return appConfigurationRepository.save(configuration);
  }

  /** Stores a new logo and removes the previous one. */
  @Transactional
  public AppConfiguration replaceLogo(long expectedVersion, byte[] data) {
    String contentType = checkLogo(data);
    String sha256 = sha256Hex(data);

    AppConfiguration configuration = lockForChange(expectedVersion);
    if (!appConfigurationLogoRepository.existsById(sha256)) {
      appConfigurationLogoRepository.save(new AppConfigurationLogo(sha256, contentType, data));
    }

    final String previous = configuration.getLogoSha256();
    configuration.setLogo(sha256, contentType, data.length);
    configuration.markModified();
    AppConfiguration saved = appConfigurationRepository.save(configuration);

    if (previous != null && !previous.equals(sha256)) {
      deleteLogo(previous);
    }
    return saved;
  }

  /** Drops the logo, so the UI shows its built-in one. */
  @Transactional
  public AppConfiguration removeLogo(long expectedVersion) {
    AppConfiguration configuration = lockForChange(expectedVersion);
    String previous = configuration.getLogoSha256();

    configuration.clearLogo();
    configuration.markModified();
    AppConfiguration saved = appConfigurationRepository.save(configuration);

    if (previous != null) {
      deleteLogo(previous);
    }
    return saved;
  }

  private AppConfiguration lockForChange(long expectedVersion) {
    AppConfiguration configuration = appConfigurationRepository.findByIdForUpdate(SINGLETON_ID)
        .orElseGet(AppConfiguration::newDefault);

    if (!configuration.isAtVersion(expectedVersion)) {
      throw new VersionMismatchException(new Message(
          AppConfigurationMessageKeys.ERROR_VERSION_MISMATCH,
          expectedVersion, configuration.getVersion()));
    }
    return configuration;
  }

  private String checkLogo(byte[] data) {
    if (data == null || data.length == 0) {
      throw new ValidationMessageException(AppConfigurationMessageKeys.ERROR_LOGO_EMPTY);
    }
    if (data.length > AppConfiguration.MAX_LOGO_SIZE) {
      throw new ValidationMessageException(new Message(
          AppConfigurationMessageKeys.ERROR_LOGO_TOO_LARGE, AppConfiguration.MAX_LOGO_SIZE));
    }
    return ImageTypeDetector.detect(data).orElseThrow(() ->
        new ValidationMessageException(AppConfigurationMessageKeys.ERROR_LOGO_TYPE_INVALID));
  }

  private void deleteLogo(String sha256) {
    try {
      appConfigurationLogoRepository.deleteById(sha256);
    } catch (EmptyResultDataAccessException ex) {
      LOGGER.debug("Logo {} was already removed", sha256, ex);
    }
  }

  private static String sha256Hex(byte[] data) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}

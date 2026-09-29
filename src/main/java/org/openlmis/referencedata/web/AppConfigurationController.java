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

import static org.openlmis.referencedata.web.AppConfigurationController.RESOURCE_PATH;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.AppConfiguration;
import org.openlmis.referencedata.domain.AppConfigurationLogo;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.AppConfigurationDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.PreconditionRequiredException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.AppConfigurationService;
import org.openlmis.referencedata.util.messagekeys.AppConfigurationMessageKeys;
import org.openlmis.referencedata.validate.AppConfigurationValidator;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@RestController
@RequestMapping(RESOURCE_PATH)
public class AppConfigurationController extends BaseController {
  public static final String RESOURCE_PATH = API_PATH + "/appConfiguration";
  public static final String LOGO_URL = "/logo";
  public static final String AUDIT_LOG_URL = "/auditLog";

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(AppConfiguration.class);
  private static final String WEAK_PREFIX = "W/";
  private static final String LOGO_CACHE_FOREVER = "public, max-age=31536000, immutable";

  @Autowired
  private AppConfigurationService appConfigurationService;

  @Autowired
  private AppConfigurationValidator appConfigurationValidator;

  /** Public, since the sign-in page shows the logo and name. */
  @GetMapping
  public ResponseEntity<AppConfigurationDto> getAppConfiguration(
      @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
    Profiler profiler = new Profiler("GET_APP_CONFIGURATION");
    profiler.setLogger(XLOGGER);

    profiler.start("FIND_APP_CONFIGURATION");
    AppConfiguration configuration = appConfigurationService.get();
    String etag = etagFor(configuration);

    ResponseEntity<AppConfigurationDto> response = matchesAny(ifNoneMatch, configuration)
        ? ResponseEntity.status(HttpStatus.NOT_MODIFIED)
            .eTag(etag).cacheControl(CacheControl.noCache()).build()
        : ResponseEntity.ok()
            .eTag(etag).cacheControl(CacheControl.noCache()).body(toDto(configuration));

    profiler.stop().log();
    return response;
  }

  /** Replaces the name, theme and feature flags. */
  @PutMapping
  public ResponseEntity<AppConfigurationDto> updateAppConfiguration(
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody AppConfigurationDto appConfigurationDto, BindingResult bindingResult) {
    Profiler profiler = new Profiler("UPDATE_APP_CONFIGURATION");
    profiler.setLogger(XLOGGER);

    checkAdminRight(RightName.SYSTEM_SETTINGS_MANAGE, profiler);
    final long expectedVersion = expectedVersion(ifMatch, appConfigurationDto.getVersion());

    profiler.start("VALIDATE");
    appConfigurationValidator.validate(appConfigurationDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);

    profiler.start("SAVE");
    AppConfiguration saved = appConfigurationService.update(expectedVersion, appConfigurationDto);

    profiler.stop().log();
    return withETag(saved);
  }

  /** Stores a new logo. */
  @PutMapping(value = LOGO_URL, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AppConfigurationDto> uploadLogo(
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestPart("file") MultipartFile file) throws IOException {
    Profiler profiler = new Profiler("UPLOAD_APP_CONFIGURATION_LOGO");
    profiler.setLogger(XLOGGER);

    checkAdminRight(RightName.SYSTEM_SETTINGS_MANAGE, profiler);
    long expectedVersion = expectedVersion(ifMatch, null);

    profiler.start("SAVE");
    AppConfiguration saved = appConfigurationService.replaceLogo(expectedVersion, file.getBytes());

    profiler.stop().log();
    return withETag(saved);
  }

  /** Drops the logo, so the UI shows its built-in one. */
  @DeleteMapping(LOGO_URL)
  public ResponseEntity<AppConfigurationDto> removeLogo(
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
    Profiler profiler = new Profiler("REMOVE_APP_CONFIGURATION_LOGO");
    profiler.setLogger(XLOGGER);

    checkAdminRight(RightName.SYSTEM_SETTINGS_MANAGE, profiler);
    long expectedVersion = expectedVersion(ifMatch, null);

    profiler.start("SAVE");
    AppConfiguration saved = appConfigurationService.removeLogo(expectedVersion);

    profiler.stop().log();
    return withETag(saved);
  }

  /** Cacheable for good when v is the current hash, since a new logo gets a new URL. */
  @GetMapping(LOGO_URL)
  public ResponseEntity<byte[]> getLogo(
      @RequestParam(value = "v", required = false) String requestedHash) {
    Profiler profiler = new Profiler("GET_APP_CONFIGURATION_LOGO");
    profiler.setLogger(XLOGGER);

    profiler.start("FIND_LOGO");
    AppConfigurationLogo logo = appConfigurationService.getLogo().orElseThrow(() ->
        new NotFoundException(AppConfigurationMessageKeys.ERROR_LOGO_NOT_FOUND));

    String cacheControl = logo.getSha256().equals(requestedHash)
        ? LOGO_CACHE_FOREVER
        : CacheControl.noCache().getHeaderValue();

    profiler.stop().log();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(logo.getContentType()))
        .eTag(logo.getSha256())
        .header(HttpHeaders.CACHE_CONTROL, cacheControl)
        .body(logo.getData());
  }

  /** Returns the change history. */
  @GetMapping(AUDIT_LOG_URL)
  public ResponseEntity<String> getAuditLog(
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable pageable) {
    Profiler profiler = new Profiler("GET_APP_CONFIGURATION_AUDIT_LOG");
    profiler.setLogger(XLOGGER);

    checkAdminRight(RightName.SYSTEM_SETTINGS_MANAGE, profiler);

    profiler.start("GET_AUDIT_LOG");
    ResponseEntity<String> response = getAuditLogResponse(AppConfiguration.class,
        AppConfiguration.SINGLETON_ID, author, changedPropertyName, pageable, returnJson);

    profiler.stop().log();
    return response;
  }

  private ResponseEntity<AppConfigurationDto> withETag(AppConfiguration configuration) {
    return ResponseEntity.ok().eTag(etagFor(configuration)).body(toDto(configuration));
  }

  private AppConfigurationDto toDto(AppConfiguration configuration) {
    AppConfigurationDto dto = new AppConfigurationDto();
    configuration.export(dto);
    return dto;
  }

  private static String etagFor(AppConfiguration configuration) {
    return WEAK_PREFIX + "\"" + configuration.getVersion() + "\"";
  }

  private static boolean matchesAny(String ifNoneMatch, AppConfiguration configuration) {
    if (StringUtils.isBlank(ifNoneMatch)) {
      return false;
    }
    for (String tag : ifNoneMatch.split(",")) {
      String trimmed = tag.trim();
      if ("*".equals(trimmed) || String.valueOf(configuration.getVersion())
          .equals(unwrap(trimmed))) {
        return true;
      }
    }
    return false;
  }

  private static long expectedVersion(String ifMatch, Long bodyVersion) {
    if (StringUtils.isNotBlank(ifMatch)) {
      try {
        return Long.parseLong(unwrap(ifMatch.trim()));
      } catch (NumberFormatException ex) {
        throw new ValidationMessageException(ex, AppConfigurationMessageKeys.ERROR_VERSION_INVALID);
      }
    }
    if (bodyVersion != null) {
      return bodyVersion;
    }
    throw new PreconditionRequiredException(AppConfigurationMessageKeys.ERROR_VERSION_REQUIRED);
  }

  private static String unwrap(String tag) {
    String value = tag.startsWith(WEAK_PREFIX) ? tag.substring(WEAK_PREFIX.length()) : tag;
    return StringUtils.strip(value, "\"");
  }
}

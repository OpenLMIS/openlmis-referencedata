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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.referencedata.domain.AppConfiguration;
import org.openlmis.referencedata.domain.AppConfigurationLogo;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.AppConfigurationDto;
import org.openlmis.referencedata.util.messagekeys.AppConfigurationMessageKeys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class AppConfigurationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_PATH = AppConfigurationController.RESOURCE_PATH;
  private static final String LOGO_PATH = RESOURCE_PATH + AppConfigurationController.LOGO_URL;
  private static final String AUDIT_LOG_PATH =
      RESOURCE_PATH + AppConfigurationController.AUDIT_LOG_URL;
  private static final String FILE = "file";
  private static final String APP_NAME = "SIGECA";
  private static final String CURRENT_VERSION = "W/\"3\"";
  private static final String VERSION = "version";
  private static final String APP_NAME_PATH = "appName";
  private static final String PNG_TYPE = "image/png";
  private static final String LOGO_TYPE_PATH = "logo.contentType";
  private static final String PNG_FILE = "logo.png";
  private static final String PNG_SHA256 =
      "a1d1ab8ebc1ca4c8d7ca9e1b1c3cbb6ca2a8f7a4b2f5f7d3d2b2f59e3b2c4d1e";

  private static final byte[] PNG = {
      (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
  };

  private AppConfiguration configuration;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    configuration = new AppConfiguration();
    configuration.setId(AppConfiguration.SINGLETON_ID);
    configuration.setVersion(3L);
    configuration.setAppName(APP_NAME);
    configuration.setThemePreset("teal");
    configuration.setDefaultAppearance("dark");
    Map<String, Object> flags = new LinkedHashMap<>();
    flags.put("BATCH_APPROVE_SCREEN", true);
    configuration.setFeatureFlags(flags);

    given(appConfigurationRepository.findById(AppConfiguration.SINGLETON_ID))
        .willReturn(Optional.of(configuration));
    given(appConfigurationRepository.findByIdForUpdate(AppConfiguration.SINGLETON_ID))
        .willReturn(Optional.of(configuration));
    given(appConfigurationRepository.save(any(AppConfiguration.class)))
        .willAnswer(new SaveAnswer<>());
    given(appConfigurationLogoRepository.save(any(AppConfigurationLogo.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    mockUserHasRight(RightName.SYSTEM_SETTINGS_MANAGE);
  }

  // GET /api/appConfiguration

  @Test
  public void shouldReturnConfigurationWithoutToken() {
    restAssured.given()
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .header(HttpHeaders.ETAG, CURRENT_VERSION)
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .body(VERSION, is(3))
        .body(APP_NAME_PATH, is(APP_NAME))
        .body("theme.preset", is("teal"))
        .body("theme.defaultAppearance", is("dark"))
        .body("featureFlags.BATCH_APPROVE_SCREEN", is(true))
        .body("logo", is(nullValue()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotModifiedWhenETagMatches() {
    restAssured.given()
        .header(HttpHeaders.IF_NONE_MATCH, CURRENT_VERSION)
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(304)
        .header(HttpHeaders.ETAG, CURRENT_VERSION);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnConfigurationWhenETagIsStale() {
    restAssured.given()
        .header(HttpHeaders.IF_NONE_MATCH, "W/\"2\"")
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .body(VERSION, is(3));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnDefaultsWhenNothingIsStored() {
    given(appConfigurationRepository.findById(AppConfiguration.SINGLETON_ID))
        .willReturn(Optional.empty());

    restAssured.given()
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .body(VERSION, is(0))
        .body(APP_NAME_PATH, is(nullValue()))
        .body("theme.preset", is(nullValue()))
        .body("featureFlags.size()", is(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPointLogoUrlAtContentHash() {
    configuration.setLogo(PNG_SHA256, PNG_TYPE, PNG.length);

    restAssured.given()
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .body("logo.url", is(LOGO_PATH + "?v=" + PNG_SHA256))
        .body(LOGO_TYPE_PATH, is(PNG_TYPE))
        .body("logo.size", is(PNG.length));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // PUT /api/appConfiguration

  @Test
  public void shouldUpdateConfiguration() {
    AppConfigurationDto body = bodyWith("Malawi OpenLMIS");

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .header(HttpHeaders.ETAG, "W/\"4\"")
        .body(VERSION, is(4))
        .body(APP_NAME_PATH, is("Malawi OpenLMIS"))
        .body("theme.preset", is("green"))
        .body("featureFlags.GS1_SCANNING", is(true))
        .body("featureFlags.BATCH_APPROVE_SCREEN", is(nullValue()))
        .body("modifiedDate", is(notNullValue()));

    ArgumentCaptor<AppConfiguration> saved = ArgumentCaptor.forClass(AppConfiguration.class);
    verify(appConfigurationRepository).save(saved.capture());
    assertEquals("Malawi OpenLMIS", saved.getValue().getAppName());
    assertEquals(Long.valueOf(4), saved.getValue().getVersion());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldTrimAppNameOnUpdate() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(bodyWith("  SIGECA  "))
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .body(APP_NAME_PATH, is(APP_NAME));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUseBodyVersionWhenIfMatchIsMissing() {
    AppConfigurationDto body = bodyWith(APP_NAME);
    body.setVersion(3L);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(200)
        .body(VERSION, is(4));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectStaleVersionOnUpdate() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, "W/\"2\"")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(bodyWith(APP_NAME))
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(409)
        .body(MESSAGE_KEY, is(AppConfigurationMessageKeys.ERROR_VERSION_MISMATCH));

    verify(appConfigurationRepository, never()).save(any(AppConfiguration.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRequireVersionOnUpdate() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(bodyWith(APP_NAME))
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(428)
        .body(MESSAGE_KEY, is(AppConfigurationMessageKeys.ERROR_VERSION_REQUIRED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectInvalidConfiguration() {
    AppConfigurationDto body = bodyWith(APP_NAME);
    body.setTheme(new AppConfigurationDto.ThemeDto("green", "sepia"));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, is(AppConfigurationMessageKeys.ERROR_THEME_APPEARANCE_INVALID));

    verify(appConfigurationRepository, never()).save(any(AppConfiguration.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdateWithoutRight() {
    mockUserHasNoRight(RightName.SYSTEM_SETTINGS_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(bodyWith(APP_NAME))
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(403);

    verify(appConfigurationRepository, never()).save(any(AppConfiguration.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(bodyWith(APP_NAME))
        .when()
        .put(RESOURCE_PATH)
        .then()
        .statusCode(401);

    verify(appConfigurationRepository, never()).save(any(AppConfiguration.class));
  }

  // PUT and DELETE /api/appConfiguration/logo

  @Test
  public void shouldUploadLogo() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, PNG_FILE, PNG, PNG_TYPE)
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(200)
        .header(HttpHeaders.ETAG, "W/\"4\"")
        .body(VERSION, is(4))
        .body(LOGO_TYPE_PATH, is(PNG_TYPE))
        .body("logo.size", is(PNG.length))
        .body("logo.url", containsString(LOGO_PATH + "?v="));

    ArgumentCaptor<AppConfigurationLogo> saved =
        ArgumentCaptor.forClass(AppConfigurationLogo.class);
    verify(appConfigurationLogoRepository).save(saved.capture());
    assertArrayEquals(PNG, saved.getValue().getData());
    assertEquals(64, saved.getValue().getSha256().length());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDetectLogoTypeFromBytesNotDeclaredType() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, "logo.jpg", PNG, "image/jpeg")
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(200)
        .body(LOGO_TYPE_PATH, is(PNG_TYPE));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeletePreviousLogoWhenReplaced() {
    configuration.setLogo(PNG_SHA256, PNG_TYPE, PNG.length);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, "logo.webp", webp(), "image/webp")
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(200)
        .body(LOGO_TYPE_PATH, is("image/webp"));

    verify(appConfigurationLogoRepository).deleteById(PNG_SHA256);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectSvgLogo() {
    byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, "logo.svg", svg, "image/svg+xml")
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, is(AppConfigurationMessageKeys.ERROR_LOGO_TYPE_INVALID));

    verify(appConfigurationLogoRepository, never()).save(any(AppConfigurationLogo.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectTooLargeLogo() {
    byte[] large = Arrays.copyOf(PNG, AppConfiguration.MAX_LOGO_SIZE + 1);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, PNG_FILE, large, PNG_TYPE)
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, is(AppConfigurationMessageKeys.ERROR_LOGO_TOO_LARGE));

    verify(appConfigurationLogoRepository, never()).save(any(AppConfigurationLogo.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectStaleVersionOnLogoUpload() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, "W/\"1\"")
        .multiPart(FILE, PNG_FILE, PNG, PNG_TYPE)
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(409);

    verify(appConfigurationLogoRepository, never()).save(any(AppConfigurationLogo.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectLogoUploadWithoutRight() {
    mockUserHasNoRight(RightName.SYSTEM_SETTINGS_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .multiPart(FILE, PNG_FILE, PNG, PNG_TYPE)
        .when()
        .put(LOGO_PATH)
        .then()
        .statusCode(403);

    verify(appConfigurationLogoRepository, never()).save(any(AppConfigurationLogo.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRemoveLogo() {
    configuration.setLogo(PNG_SHA256, PNG_TYPE, PNG.length);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .when()
        .delete(LOGO_PATH)
        .then()
        .statusCode(200)
        .body(VERSION, is(4))
        .body("logo", is(nullValue()));

    verify(appConfigurationLogoRepository).deleteById(PNG_SHA256);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectLogoRemovalWithoutRight() {
    mockUserHasNoRight(RightName.SYSTEM_SETTINGS_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.IF_MATCH, CURRENT_VERSION)
        .when()
        .delete(LOGO_PATH)
        .then()
        .statusCode(403);

    verify(appConfigurationLogoRepository, never()).deleteById(anyString());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/appConfiguration/logo

  @Test
  public void shouldServeLogoWithoutToken() {
    configuration.setLogo(PNG_SHA256, PNG_TYPE, PNG.length);
    given(appConfigurationLogoRepository.findById(PNG_SHA256))
        .willReturn(Optional.of(new AppConfigurationLogo(PNG_SHA256, PNG_TYPE, PNG)));

    Response response = restAssured.given()
        .queryParam("v", PNG_SHA256)
        .when()
        .get(LOGO_PATH)
        .then()
        .statusCode(200)
        .contentType(PNG_TYPE)
        .header(HttpHeaders.CACHE_CONTROL, containsString("immutable"))
        .header(HttpHeaders.ETAG, "\"" + PNG_SHA256 + "\"")
        .header("X-Content-Type-Options", "nosniff")
        .extract().response();

    assertArrayEquals(PNG, response.asByteArray());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotCacheLogoForeverWhenHashIsOutdated() {
    configuration.setLogo(PNG_SHA256, PNG_TYPE, PNG.length);
    given(appConfigurationLogoRepository.findById(PNG_SHA256))
        .willReturn(Optional.of(new AppConfigurationLogo(PNG_SHA256, PNG_TYPE, PNG)));

    restAssured.given()
        .queryParam("v", "outdated")
        .when()
        .get(LOGO_PATH)
        .then()
        .statusCode(200)
        .header(HttpHeaders.CACHE_CONTROL, not(containsString("immutable")));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenThereIsNoLogo() {
    restAssured.given()
        .when()
        .get(LOGO_PATH)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/appConfiguration/auditLog

  @Test
  public void shouldReturnAuditLog() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(AUDIT_LOG_PATH)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectAuditLogWithoutRight() {
    mockUserHasNoRight(RightName.SYSTEM_SETTINGS_MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(AUDIT_LOG_PATH)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private AppConfigurationDto bodyWith(String appName) {
    AppConfigurationDto body = new AppConfigurationDto();
    body.setAppName(appName);
    body.setTheme(new AppConfigurationDto.ThemeDto("green", "system"));
    body.setFeatureFlags(Collections.singletonMap("GS1_SCANNING", true));
    return body;
  }

  private byte[] webp() {
    return new byte[]{
        'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '
    };
  }
}

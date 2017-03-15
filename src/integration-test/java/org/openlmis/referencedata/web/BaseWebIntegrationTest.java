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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.SystemMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;

import javax.annotation.PostConstruct;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
public abstract class BaseWebIntegrationTest {

  protected static final String ACCESS_TOKEN = "access_token";

  private static final String USER_ACCESS_TOKEN = "418c89c5-7f21-4cd1-a63a-38c47892b0fe";
  private static final String CLIENT_ACCESS_TOKEN = "6d6896a5-e94c-4183-839d-911bc63174ff";

  protected static final String MESSAGEKEY_ERROR_UNAUTHORIZED =
      SystemMessageKeys.ERROR_UNAUTHORIZED;

  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";
  
  static final String MESSAGE_KEY = "messageKey";

  protected RestAssuredClient restAssured;

  private static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml").ignoringXheaders();

  private static final String MOCK_USER_CHECK_RESULT = "{\n"
      + "  \"aud\": [\n"
      + "    \"referencedata\"\n"
      + "  ],\n"
      + "  \"user_name\": \"admin\",\n"
      + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\",\n"
      + "  \"scope\": [\"read\", \"write\"],\n"
      + "  \"exp\": 1474500343,\n"
      + "  \"authorities\": [\"USER\", \"ADMIN\"],\n"
      + "  \"client_id\": \"user-client\"\n"
      + "}";

  private static final String MOCK_CLIENT_CHECK_RESULT = "{\n"
      + "  \"aud\": [\n"
      + "    \"referencedata\"\n"
      + "  ],\n"
      + "  \"scope\": [\"read\", \"write\"],\n"
      + "  \"exp\": 1474500343,\n"
      + "  \"authorities\": [\"TRUSTED_CLIENT\"],\n"
      + "  \"client_id\": \"trusted-client\"\n"
      + "}";

  @Value("${auth.server.baseUrl}")
  protected String baseUri;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  @LocalServerPort
  private int randomPort;

  @SpyBean
  protected RightService rightService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Constructor for test.
   */
  public BaseWebIntegrationTest() {

    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
        .withRequestBody(equalTo("token=" + USER_ACCESS_TOKEN))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(MOCK_USER_CHECK_RESULT)));

    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
        .withRequestBody(equalTo("token=" + CLIENT_ACCESS_TOKEN))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(MOCK_CLIENT_CHECK_RESULT)));

    // This mocks the call to auth to post to an auth user.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/users/auth"))
        .willReturn(aResponse()
            .withStatus(200)));

    // This mocks the call to notification to post a notification.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/notification"))
        .willReturn(aResponse()
            .withStatus(200)));
  }

  /**
   * Initialize the REST Assured client. Done here and not in the constructor, so that randomPort is
   * available.
   */
  @PostConstruct
  public void init() {

    RestAssured.baseURI = baseUri;
    RestAssured.port = randomPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((clazz, charset) -> objectMapper)
    );
    restAssured = ramlDefinition.createRestAssured();
  }

  /**
   * Get a user access token. An arbitrary UUID string is returned and the tests assume it is a
   * valid one for an admin user.
   *
   * @return an access token
   */
  String getToken() {
    return USER_ACCESS_TOKEN;
  }

  /**
   * Get a trusted client access token. An arbitrary UUID string is returned and the tests assume it
   * is a valid one for a trusted client. This is for service-to-service communication.
   *
   * @return an access token
   */
  String getClientToken() {
    return CLIENT_ACCESS_TOKEN;
  }

  protected void mockUserHasRight(String rightName) {
    doNothing().when(rightService).checkAdminRight(eq(rightName), anyBoolean(), any());
    doNothing().when(rightService).checkAdminRight(rightName);
  }

  protected void mockUserHasNoRight(String rightName) {
    mockUserHasNoRight(rightName, null);
  }

  protected void mockUserHasNoRight(String rightName, UUID userId) {
    Message message = new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName);
    UnauthorizedException exception = new UnauthorizedException(message);

    doThrow(exception).when(rightService).checkAdminRight(rightName);
    doThrow(exception).when(rightService).checkAdminRight(eq(rightName), anyBoolean());
    doThrow(exception).when(rightService).checkAdminRight(eq(rightName), anyBoolean(), any());
    if (userId != null) {
      doNothing().when(rightService).checkAdminRight(eq(rightName), anyBoolean(), eq(userId));
    }
  }

}

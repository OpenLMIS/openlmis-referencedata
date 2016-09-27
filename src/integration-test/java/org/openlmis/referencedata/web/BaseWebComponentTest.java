package org.openlmis.referencedata.web;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.junit.After;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.utils.CleanRepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest("server.port:8080")
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public abstract class BaseWebComponentTest {

  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  protected RestAssuredClient restAssured;

  private static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml");

  private static final String MOCK_CHECK_RESULT = "{\n"
      + "  \"aud\": [\n"
      + "    \"auth\",\n"
      + "    \"example\",\n"
      + "    \"requisition\",\n"
      + "    \"notification\",\n"
      + "    \"referencedata\"\n"
      + "  ],\n"
      + "  \"user_name\": \"admin\",\n"
      + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\",\n"
      + "  \"scope\": [\n"
      + "    \"read\",\n"
      + "    \"write\"\n"
      + "  ],\n"
      + "  \"exp\": 1474500343,\n"
      + "  \"authorities\": [\n"
      + "    \"USER\",\n"
      + "    \"ADMIN\"\n"
      + "  ],\n"
      + "  \"client_id\": \"trusted-client\"\n"
      + "}";

  @Autowired
  private CleanRepositoryHelper cleanRepositoryHelper;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  /**
   * Constructor for test.
   */
  public BaseWebComponentTest() {
    String virtualHost = Optional.ofNullable(System.getenv("VIRTUAL_HOST")).orElse("localhost");
    RestAssured.baseURI = "http://" + virtualHost + ":8080";
    restAssured = ramlDefinition.createRestAssured();

    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/auth/oauth/check_token"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(MOCK_CHECK_RESULT)));

    // This mocks the call to auth to post to an auth user.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/users"))
        .willReturn(aResponse()
            .withStatus(200)));

    // This mocks the call to notification to post a notification.
    wireMockRule.stubFor(post(urlPathEqualTo("/notification"))
        .willReturn(aResponse()
            .withStatus(200)));
  }

  @After
  public void cleanRepositories() {
    cleanRepositoryHelper.cleanAll();
  }

  /**
   * Get an access token. An arbitrary UUID string is returned and the tests assume it is a valid
   * one.
   *
   * @return an access token
   */
  String getToken() {
    return "418c89c5-7f21-4cd1-a63a-38c47892b0fe";
  }
}

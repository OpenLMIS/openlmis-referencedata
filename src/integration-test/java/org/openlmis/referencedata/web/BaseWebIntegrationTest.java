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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.filter.log.LogDetail;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.RightAssignmentRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleAssignmentRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.ServiceAccountRepository;
import org.openlmis.referencedata.repository.StockAdjustmentReasonRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.service.FacilityService;
import org.openlmis.referencedata.service.GeographicZoneService;
import org.openlmis.referencedata.service.OrderableService;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.service.RequisitionGroupService;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.service.SupervisoryNodeService;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.SystemMessageKeys;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;

import java.util.UUID;

import javax.annotation.PostConstruct;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({"PMD.TooManyMethods"})
public abstract class BaseWebIntegrationTest {

  private static final String USER_ACCESS_TOKEN = "418c89c5-7f21-4cd1-a63a-38c47892b0fe";
  private static final String USER_ACCESS_TOKEN_HEADER = "Bearer " + USER_ACCESS_TOKEN;
  private static final String CLIENT_ACCESS_TOKEN = "6d6896a5-e94c-4183-839d-911bc63174ff";
  private static final String CLIENT_ACCESS_TOKEN_HEADER = "Bearer " + CLIENT_ACCESS_TOKEN;


  protected static final String MESSAGEKEY_ERROR_UNAUTHORIZED =
      SystemMessageKeys.ERROR_UNAUTHORIZED;
  protected static final String MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC =
      SystemMessageKeys.ERROR_UNAUTHORIZED_GENERIC;

  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  static final String MESSAGE_KEY = "messageKey";
  static final String MESSAGE = "message";

  static final String ID = "id";

  protected RestAssuredClient restAssured;

  private static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml").ignoringXheaders();

  private static final UUID ADMIN_ID = UUID.fromString("35316636-6264-6331-2d34-3933322d3462");

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

  @MockBean
  protected CommodityTypeRepository commodityTypeRepository;

  @MockBean
  protected TradeItemRepository tradeItemRepository;

  @MockBean
  protected OrderableRepository orderableRepository;

  @MockBean
  protected FacilityRepository facilityRepository;

  @MockBean
  protected FacilityService facilityService;

  @MockBean
  protected SupplyLineService supplyLineService;

  @MockBean
  protected ProgramRepository programRepository;

  @MockBean
  protected FacilityTypeApprovedProductRepository facilityTypeApprovedProductRepository;

  @MockBean
  protected SupervisoryNodeRepository supervisoryNodeRepository;

  @MockBean
  protected FacilityOperatorRepository facilityOperatorRepository;

  @MockBean
  protected OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @MockBean
  protected FacilityTypeRepository facilityTypeRepository;

  @MockBean
  protected GeographicLevelRepository geographicLevelRepository;

  @MockBean
  protected GeographicZoneRepository geographicZoneRepository;

  @MockBean
  protected GeographicZoneService geographicZoneService;

  @MockBean
  protected LotRepository lotRepository;

  @MockBean
  protected OrderableService orderableService;

  @MockBean
  protected ProcessingPeriodRepository periodRepository;

  @MockBean
  protected ProcessingScheduleRepository scheduleRepository;

  @MockBean
  protected RequisitionGroupProgramScheduleRepository requisitionGroupProgramScheduleRepository;

  @MockBean(name = "beforeSavePeriodValidator")
  protected ProcessingPeriodValidator periodValidator;

  @MockBean
  protected RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  @MockBean
  protected RequisitionGroupRepository requisitionGroupRepository;

  @MockBean
  protected RequisitionGroupService requisitionGroupService;

  @MockBean
  protected RequisitionGroupValidator requisitionGroupValidator;

  @MockBean
  protected RightRepository rightRepository;

  @MockBean
  protected RoleRepository roleRepository;

  @MockBean
  protected StockAdjustmentReasonRepository stockAdjustmentReasonRepository;

  @MockBean
  protected UserRepository userRepository;

  @MockBean
  protected SupervisoryNodeService supervisoryNodeService;

  @MockBean
  protected SupplyLineRepository supplyLineRepository;

  @MockBean
  protected UserService userService;
  
  @MockBean
  protected RightAssignmentRepository rightAssignmentRepository;
  
  @MockBean
  protected RoleAssignmentRepository roleAssignmentRepository;

  @MockBean
  protected IdealStockAmountRepository idealStockAmountRepository;

  @MockBean
  protected ServiceAccountRepository serviceAccountRepository;

  @MockBean
  protected AuthenticationHelper authenticationHelper;

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
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    restAssured = ramlDefinition.createRestAssured();
  }

  @Before
  public void setUp() {
    // by default user has no access to resources
    given(userRepository.exists(ADMIN_ID)).willReturn(true);
    given(rightAssignmentRepository.existsByUserIdAndRightName(eq(ADMIN_ID), anyString()))
        .willReturn(false);

    mockUserAuthenticated();
  }

  /**
   * Get a user access token. An arbitrary UUID string is returned and the tests assume it is a
   * valid one for an admin user.
   *
   * @return an access token
   */
  String getTokenHeader() {
    return USER_ACCESS_TOKEN_HEADER;
  }

  /**
   * Get a trusted client access token. An arbitrary UUID string is returned and the tests assume it
   * is a valid one for a trusted client. This is for service-to-service communication.
   *
   * @return an access token
   */
  String getClientTokenHeader() {
    return CLIENT_ACCESS_TOKEN_HEADER;
  }

  protected void mockUserAuthenticated() {
    User user = new UserDataBuilder().buildAsNew();
    user.setId(ADMIN_ID);

    given(authenticationHelper.getCurrentUser()).willReturn(user);
  }

  protected void mockUserHasRight(String rightName) {
    doNothing().when(rightService).checkAdminRight(eq(rightName), anyBoolean(), any(UUID.class));
    doNothing().when(rightService).checkAdminRight(rightName);
  }

  protected void mockUserHasNoRight(String rightName) {
    mockUserHasNoRight(rightName, null);
  }

  protected void mockUserHasNoRight(String rightName, UUID userId) {
    Message message = new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName);
    UnauthorizedException exception = new UnauthorizedException(message);

    doThrow(exception).when(rightService).checkAdminRight(rightName);
    doThrow(exception).when(rightService).checkAdminRight(eq(rightName), anyBoolean(), any());
    if (userId != null) {
      doNothing().when(rightService).checkAdminRight(eq(rightName), anyBoolean(), eq(userId));
    }
  }
  
  protected void mockClientHasRootAccess() {
    doNothing().when(rightService).checkRootAccess();
  }
  
  protected void mockClientHasNoRootAccess() {
    UnauthorizedException exception = new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC));
    doThrow(exception).when(rightService).checkRootAccess();
  }

  void checkBadRequestBody(Object object, String code, String resourceUrl) {
    ExtractableResponse<Response> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(object)
        .when()
        .put(resourceUrl)
        .then()
        .statusCode(400)
        .extract();

    String messageKey = response.path(MESSAGE_KEY);
    String message = response.path(MESSAGE);

    assertThat(messageKey, is(Matchers.equalTo(code)));
    assertFalse(message.equals(messageKey));
  }

  void checkPageBody(ValidatableResponse response, int page, int size, int numberOfElements,
                     int totalElements, int totalPages) {
    response
        .body("number", is(page))
        .body("size", is(size))
        .body("numberOfElements", is(numberOfElements))
        .body("content.size()", is(numberOfElements))
        .body("totalElements", is(totalElements))
        .body("totalPages", is(totalPages));
  }

  RequestSpecification startRequest(String token) {
    RequestSpecification request = restAssured
        .given()
        .log()
        .ifValidationFails(LogDetail.ALL, true)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    if (null != token) {
      request = request.header(HttpHeaders.AUTHORIZATION, token);
    }

    return request;
  }

  static class SaveAnswer<T extends BaseEntity> implements Answer<T> {

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
      T obj = (T) invocation.getArguments()[0];

      if (null == obj) {
        return null;
      }

      if (null == obj.getId()) {
        obj.setId(UUID.randomUUID());
      }

      return obj;
    }

  }
}

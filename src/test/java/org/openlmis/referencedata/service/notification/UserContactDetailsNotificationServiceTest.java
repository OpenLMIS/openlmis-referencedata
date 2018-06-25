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

package org.openlmis.referencedata.service.notification;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class UserContactDetailsNotificationServiceTest {
  private static final String TOKEN = UUID.randomUUID().toString();
  protected static final String TOKEN_HEADER = "Bearer " + TOKEN;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AuthService authService;

  @InjectMocks
  private UserContactDetailsNotificationService service;

  @Captor
  private ArgumentCaptor<URI> uriCaptor;

  @Captor
  private ArgumentCaptor<HttpEntity<UserContactDetailsDto>> entityCaptor;

  private UserContactDetailsDto userContactDetails;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(service, "serviceUrl", "http://localhost/notification");
    when(authService.obtainAccessToken()).thenReturn(TOKEN);

    UserDto userDto = new UserDto();
    new UserDataBuilder().withEmail(null).build().export(userDto);

    userContactDetails = new UserContactDetailsDto(userDto);
  }

  @Test
  public void shouldSendNotification() {
    service.putContactDetails(userContactDetails);

    verify(restTemplate).postForEntity(
        uriCaptor.capture(),
        entityCaptor.capture(),
        eq(UserContactDetailsDto.class)
    );

    URI uri = uriCaptor.getValue();
    String url = "http://localhost/notification/api/userContactDetails/"
        + userContactDetails.getReferenceDataUserId();
    assertThat(uri.toString(), is(url));

    HttpEntity entity = entityCaptor.getValue();
    Object body = entity.getBody();

    assertThat(body, instanceOf(UserContactDetailsDto.class));

    UserContactDetailsDto sent = (UserContactDetailsDto) body;

    assertThat(sent, is(userContactDetails));

    assertAuthHeader(entity);
  }

  @Test(expected = HttpServerErrorException.class)
  public void shouldReturnFalseIfCannotSendNotification() {
    when(restTemplate
        .postForEntity(any(URI.class), any(HttpEntity.class), eq(UserContactDetailsDto.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

    service.putContactDetails(userContactDetails);
  }

  private void assertAuthHeader(HttpEntity value) {
    List<String> authorization = value.getHeaders().get(HttpHeaders.AUTHORIZATION);

    assertThat(authorization, hasSize(1));
    assertThat(authorization, hasItem(TOKEN_HEADER));
  }

}

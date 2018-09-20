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

package org.openlmis.referencedata.fhir;

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.service.AuthService;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.UnusedPrivateField")
public class AuthorizationFactoryTest {
  private static final String TOKEN = "4718a717-d3e8-483c-968a-470a2d5efb80";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password";

  @Mock
  private AuthService authService;

  @InjectMocks
  private AuthorizationFactory factory;

  @Test
  public void shouldBuildInstanceForGenerateTokenMode() {
    factory.setMode(AuthorizationMode.GENERATE_TOKEN.name());

    Optional<IClientInterceptor> instance = factory.build();

    assertThat(instance.isPresent()).isTrue();
    assertThat(instance.get()).isInstanceOf(DynamicBearerTokenAuthInterceptor.class);
  }

  @Test
  public void shouldBuildInstanceForUseExistingTokenMode() {
    factory.setMode(AuthorizationMode.USE_EXISTING_TOKEN.name());
    factory.setToken(TOKEN);

    Optional<IClientInterceptor> instance = factory.build();

    assertThat(instance.isPresent()).isTrue();
    assertThat(instance.get()).isInstanceOf(BearerTokenAuthInterceptor.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfTokenIsNotSetForUseExistingTokenMode() {
    factory.setMode(AuthorizationMode.USE_EXISTING_TOKEN.name());
    factory.setToken(null);

    factory.build();
  }

  @Test
  public void shouldBuildInstanceForBasicAuthMode() {
    factory.setMode(AuthorizationMode.BASIC_AUTH.name());
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);

    Optional<IClientInterceptor> instance = factory.build();

    assertThat(instance.isPresent()).isTrue();
    assertThat(instance.get()).isInstanceOf(BasicAuthInterceptor.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfUsernameIsNotSetForBasicAuthMode() {
    factory.setMode(AuthorizationMode.BASIC_AUTH.name());
    factory.setUsername(null);

    factory.build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfPasswordIsNotSetForBasicAuthMode() {
    factory.setMode(AuthorizationMode.BASIC_AUTH.name());
    factory.setUsername(USERNAME);
    factory.setPassword(null);

    factory.build();
  }

  @Test
  public void shouldBuildEmptyInstanceForNoneMode() {
    factory.setMode(AuthorizationMode.NONE.name());
    assertThat(factory.build().isPresent()).isFalse();
  }

  @Test
  public void shouldBuildEmptyInstanceIfModeIsNotSet() {
    factory.setMode(null);
    assertThat(factory.build().isPresent()).isFalse();
  }
}

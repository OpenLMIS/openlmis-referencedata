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

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import java.util.Optional;
import lombok.Setter;
import org.openlmis.referencedata.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fhirClient.auth")
public class AuthorizationFactory {
  private AuthorizationMode mode = AuthorizationMode.GENERATE_TOKEN;

  @Setter
  private String token;

  @Setter
  private String username;

  @Setter
  private String password;

  @Autowired
  private AuthService authService;

  /**
   * Builds an instance of {@link IClientInterceptor} which will be used for authorization based on
   * passed parameters.
   */
  public Optional<IClientInterceptor> build() {
    if (mode == AuthorizationMode.GENERATE_TOKEN) {
      return Optional.of(new DynamicBearerTokenAuthInterceptor(authService));
    }

    if (mode == AuthorizationMode.USE_EXISTING_TOKEN) {
      checkArgument(isNotBlank(token), "Token is required with USE_EXISTING_TOKEN mode");
      return Optional.of(new BearerTokenAuthInterceptor(token));
    }

    if (mode == AuthorizationMode.BASIC_AUTH) {
      checkArgument(isNotBlank(username), "Username is required with BASIC_AUTH mode");
      checkArgument(isNotBlank(password), "Password is required with BASIC_AUTH mode");
      return Optional.of(new BasicAuthInterceptor(username, password));
    }

    return Optional.empty();
  }

  public void setMode(String mode) {
    this.mode = AuthorizationMode.fromString(mode);
  }

}

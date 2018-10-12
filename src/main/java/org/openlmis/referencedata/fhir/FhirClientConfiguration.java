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

import ca.uhn.fhir.context.FhirContext;
import org.openlmis.referencedata.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
public class FhirClientConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirClientConfiguration.class);

  @Autowired
  private AuthService authService;

  @Value("${fhirClient.serverUrl}")
  private String fhirServerUrl;

  @Value("${service.url}")
  private String serviceUrl;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forDstu3();
  }

  /**
   * Creates location synchronizer based on fhir context version.
   */
  @Bean
  public LocationSynchronizer locationSynchronizer() {
    FhirContext context = fhirContext();
    return LocationSynchronizer
        .getInstance(context.getVersion().getVersion())
        .withContext(context)
        .withFhirServerUrl(fhirServerUrl)
        .withServiceUrl(serviceUrl)
        .withAuthService(authService);
  }

  /**
   * Creates location converter based on fhir context version.
   */
  @Bean
  public LocationConverter locationConverter() {
    FhirContext context = fhirContext();
    return LocationConverter
        .getInstance(context.getVersion().getVersion())
        .withServiceUrl(serviceUrl);
  }

  /**
   * Creates a default instance of {@link FhirClient} when the feature is enabled.
   */
  @Bean
  @ConditionalOnProperty(prefix = "fhirClient", name = "enabled", havingValue = "true")
  public FhirClient defaultFhirClient(LocationFactory locationFactory) {
    LOGGER.info("The FHIR feature is enabled");

    DefaultFhirClient client = new DefaultFhirClient();
    client.setApiKeyPrefix(apiKeyPrefix);
    client.setLocationConvert(locationConverter());
    client.setLocationFactory(locationFactory);
    client.setLocationSynchronizer(locationSynchronizer());

    return client;
  }

  /**
   * Creates an empty fhir client when the feature is disabled.
   */
  @Bean
  @ConditionalOnMissingBean
  public FhirClient emptyFhirClient() {
    LOGGER.info("The FHIR feature is disabled");
    return new FhirClient() {
    };
  }

}

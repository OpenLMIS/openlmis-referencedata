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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.BooleanUtils;
import org.openlmis.referencedata.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FhirClientFactory implements FactoryBean<FhirClient>, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(FhirClientFactory.class);
  static final FhirClient EMPTY_CLIENT = new FhirClient() {
  };

  @Value("${fhirClient.enabled}")
  private String fhirEnabled;

  @Value("${fhirClient.serverUrl}")
  private String fhirServerUrl;

  @Value("${service.url}")
  private String serviceUrl;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  @Autowired
  private LocationFactory locationFactory;

  @Autowired
  private AuthService authService;

  private FhirClient fhirClient;

  @Override
  public void afterPropertiesSet() {
    if (BooleanUtils.toBoolean(fhirEnabled)) {
      LOGGER.info("The FHIR feature is enabled");
      Preconditions.checkArgument(isNotBlank(fhirServerUrl), "The FHIR server url cannot be blank");

      FhirVersionEnum version = FhirVersionEnum.DSTU3;
      FhirContext context = version.newContext();

      LocationSynchronizer locationSynchronizer = LocationSynchronizer
          .getInstance(version)
          .withContext(context)
          .withFhirServerUrl(fhirServerUrl)
          .withServiceUrl(serviceUrl)
          .withAuthService(authService);

      LocationConverter locationConvert = LocationConverter
          .getInstance(version)
          .withServiceUrl(serviceUrl);

      fhirClient = new DefaultFhirClient(locationFactory, locationConvert,
          locationSynchronizer, apiKeyPrefix);
    } else {
      LOGGER.info("The FHIR feature is disabled");
      fhirClient = EMPTY_CLIENT;
    }
  }

  @Override
  public FhirClient getObject() {
    return fhirClient;
  }

  @Override
  public Class<?> getObjectType() {
    return FhirClient.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}

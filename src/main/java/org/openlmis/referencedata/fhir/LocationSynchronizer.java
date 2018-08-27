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
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import java.util.UUID;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class LocationSynchronizer<T extends IBaseResource> implements Synchronizer<Location, T> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private FhirContext context;
  private String fhirServerUrl;
  private String serviceUrl;

  static LocationSynchronizer getInstance(FhirVersionEnum version) {
    switch (version) {
      case DSTU2:
        return new Dstu2LocationSynchronizer();
      case DSTU2_HL7ORG:
        return new Dstu2Hl7OrgLocationSynchronizer();
      case DSTU2_1:
        return new Dstu21LocationSynchronizer();
      case DSTU3:
        return new Dstu3LocationSynchronizer();
      case R4:
        return new R4LocationSynchronizer();
      default:
        throw new IllegalStateException("Unsupported FHIR version: " + version.name());
    }
  }

  @Override
  public void synchronize(Location olmisLocation, T fhirLocation) {
    LoggingInterceptor loggingInterceptor = new LoggingInterceptor(true);
    loggingInterceptor.setLogger(logger);

    IGenericClient client = context.newRestfulGenericClient(fhirServerUrl);
    client.registerInterceptor(loggingInterceptor);

    ICriterion criterion = createIdentifierCriterion(serviceUrl, olmisLocation.getId());
    T existing = findResource(client, criterion);

    if (null == existing) {
      createLocation(client, fhirLocation);
    } else {
      updateLocation(client, existing, fhirLocation);
    }
  }

  LocationSynchronizer<T> withContext(FhirContext context) {
    this.context = context;
    return this;
  }

  LocationSynchronizer<T> withFhirServerUrl(String fhirServerUrl) {
    this.fhirServerUrl = fhirServerUrl;
    return this;
  }

  LocationSynchronizer<T> withServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  Logger log() {
    return logger;
  }

  abstract ICriterion createIdentifierCriterion(String system, UUID value);

  abstract T findResource(IGenericClient client, ICriterion criterion);

  void copyIdElement(T existing, T fhirLocation) {
    fhirLocation.setId(existing.getIdElement().withVersion(null));
  }

  private void createLocation(IGenericClient client, T fhirLocation) {
    client
        .create()
        .resource(fhirLocation)
        .prettyPrint()
        .encodedJson()
        .execute();
  }

  private void updateLocation(IGenericClient client, T existing, T fhirLocation) {
    copyIdElement(existing, fhirLocation);
    client
        .update()
        .resource(fhirLocation)
        .encodedJson()
        .execute();
  }

}

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

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class LocationSynchronizer<T extends IBaseResource, B extends IBaseBundle>
    implements Synchronizer<FhirLocation, T> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Setter
  private IGenericClient client;

  @Setter
  private String serviceUrl;

  private Class<T> resourceClass;
  private Class<B> bundleClass;
  private boolean removeVersion;

  LocationSynchronizer(Class<T> resourceClass, Class<B> bundleClass, boolean removeVersion) {
    this.resourceClass = resourceClass;
    this.bundleClass = bundleClass;
    this.removeVersion = removeVersion;
  }

  @Override
  public void synchronize(FhirLocation olmisLocation, T fhirLocation) {
    logger.debug("Try to find resources by criterion");
    B bundle = client
        .search()
        .forResource(resourceClass)
        .cacheControl(buildCacheControl())
        .where(buildCriterion(olmisLocation))
        .returnBundle(bundleClass)
        .execute();
    T existing = getEntry(bundle);

    if (null == existing) {
      createLocation(client, fhirLocation);
    } else {
      updateLocation(client, existing, fhirLocation);
    }
  }

  abstract T getEntry(B bundle);

  private void createLocation(IGenericClient client, T fhirLocation) {
    fhirLocation.setId((IIdType) null);
    client
        .create()
        .resource(fhirLocation)
        .prettyPrint()
        .encodedJson()
        .execute();
  }

  private void updateLocation(IGenericClient client, T existing, T fhirLocation) {
    IIdType idType = existing.getIdElement();

    if (removeVersion) {
      idType = idType.withVersion(null);
    }

    fhirLocation.setId(idType);
    client
        .update()
        .resource(fhirLocation)
        .encodedJson()
        .execute();
  }

  private ICriterion buildCriterion(FhirLocation olmisLocation) {
    logger.trace(
        "Create identifier criterion for system {} and value {}",
        serviceUrl, olmisLocation.getId()
    );

    return new TokenClientParam("identifier")
        .exactly()
        .systemAndValues(serviceUrl, olmisLocation.getId().toString());
  }

  private CacheControlDirective buildCacheControl() {
    CacheControlDirective cacheControl = new CacheControlDirective();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    cacheControl.setMaxResults(1);

    return cacheControl;
  }

}

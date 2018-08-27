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

import lombok.AllArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
class DefaultFhirClient implements FhirClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFhirClient.class);

  private LocationFactory locationFactory;
  private LocationConverter locationConvert;
  private LocationSynchronizer locationSynchronizer;

  @Override
  public void synchronizeFacility(Facility facility) {
    try {
      synchronize(locationFactory.createFor(facility));
    } catch (Exception exp) {
      LOGGER.error("Can't synchronize the facility resource", exp);
    }
  }

  @Override
  public void synchronizeGeographicZone(GeographicZone geographicZone) {
    try {
      synchronize(locationFactory.createFor(geographicZone));
    } catch (Exception exp) {
      LOGGER.error("Can't synchronize the geographic zone resource", exp);
    }
  }

  private void synchronize(Location location) {
    IBaseResource resource = locationConvert.convert(location);
    locationSynchronizer.synchronize(location, resource);
  }

}

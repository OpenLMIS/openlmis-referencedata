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

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Location;
import org.springframework.util.CollectionUtils;

class R4LocationSynchronizer extends LocationSynchronizer<Location> {

  @Override
  ICriterion createIdentifierCriterion(String system, UUID value) {
    log().trace("Create identifier criterion for system {} and value {}", system, value);
    return Location.IDENTIFIER.exactly().systemAndValues(system, value.toString());
  }

  @Override
  Location findResource(IGenericClient client, ICriterion criterion) {
    log().debug("Try to find resources by criterion");
    Bundle bundle = client
        .search()
        .forResource(Location.class)
        .where(criterion)
        .returnBundle(Bundle.class)
        .execute();

    List<BundleEntryComponent> entries = bundle.getEntry();

    if (CollectionUtils.isEmpty(entries)) {
      log().debug("Found zero resources. The null value will be returned.");
      return null;
    } else {
      log().debug("Found {} resources. The first resource will be returned.", entries.size());
      return (Location) entries.get(0).getResource();
    }
  }

}

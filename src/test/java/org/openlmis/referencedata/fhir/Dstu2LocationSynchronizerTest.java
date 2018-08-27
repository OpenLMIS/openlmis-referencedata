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

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Location;

public class Dstu2LocationSynchronizerTest extends LocationSynchronizerTest<Location, Bundle> {

  @Override
  LocationSynchronizer<Location> getSynchronizer() {
    return new Dstu2LocationSynchronizer();
  }

  @Override
  FhirVersionEnum getFhirVersion() {
    return FhirVersionEnum.DSTU2;
  }

  @Override
  Location getFhirLocation() {
    return new Location();
  }

  @Override
  Bundle getEmptyBundle() {
    return new Bundle();
  }

  @Override
  Bundle getBundle(Location resource) {
    Entry entry = new Entry();
    entry.setResource(resource);

    Bundle bundle = new Bundle();
    bundle.addEntry(entry);

    return bundle;
  }

}

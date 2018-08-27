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
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.instance.model.Location;

public class Dstu2Hl7OrgLocationSynchronizerTest
    extends LocationSynchronizerTest<Location, Bundle> {

  @Override
  LocationSynchronizer<Location, Bundle> getSynchronizer() {
    return new Dstu2Hl7OrgLocationSynchronizer();
  }

  @Override
  FhirVersionEnum getFhirVersion() {
    return FhirVersionEnum.DSTU2_HL7ORG;
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
    BundleEntryComponent entryComponent = new BundleEntryComponent();
    entryComponent.setResource(resource);

    Bundle bundle = new Bundle();
    bundle.addEntry(entryComponent);

    return bundle;
  }

}

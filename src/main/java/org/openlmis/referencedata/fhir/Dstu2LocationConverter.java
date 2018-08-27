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

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.valueset.LocationStatusEnum;
import java.math.BigDecimal;
import java.util.UUID;

class Dstu2LocationConverter extends LocationConverter<ca.uhn.fhir.model.dstu2.resource.Location> {

  @Override
  ca.uhn.fhir.model.dstu2.resource.Location createResource(Location input) {
    return new ca.uhn.fhir.model.dstu2.resource.Location();
  }

  @Override
  void setName(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    resource.setName(input.getName());
  }

  @Override
  void setPhysicalType(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    CodeableConceptDt physicalType = new CodeableConceptDt();
    input
        .getPhysicalType()
        .getCoding()
        .stream()
        .map(elem -> {
          CodingDt coding = new CodingDt();
          coding.setSystem(elem.getSystem());
          coding.setCode(elem.getCode());
          coding.setDisplay(elem.getDisplay());

          return coding;
        })
        .forEach(physicalType::addCoding);

    resource.setPhysicalType(physicalType);
  }

  @Override
  void setPartOf(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    resource.setPartOf(new ResourceReferenceDt(input.getPartOf().getReference()));
  }

  @Override
  void setIdentifier(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    input
        .getIdentifier()
        .stream()
        .map(elem -> {
          IdentifierDt identifier = new IdentifierDt();
          identifier.setSystem(elem.getSystem());
          identifier.setValue(elem.getValue());

          return identifier;
        })
        .forEach(resource::addIdentifier);
  }

  @Override
  void addSystemIdentifier(ca.uhn.fhir.model.dstu2.resource.Location resource,
      String system, UUID value) {
    IdentifierDt identifier = new IdentifierDt();
    identifier.setSystem(system);
    identifier.setValue(value.toString());

    resource.addIdentifier(identifier);
  }

  @Override
  void setAlias(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    // nothing to do here
  }

  @Override
  void setPosition(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    if (null != input.getPosition()) {
      ca.uhn.fhir.model.dstu2.resource.Location.Position position =
          new ca.uhn.fhir.model.dstu2.resource.Location.Position();
      position.setLatitude(new BigDecimal(input.getPosition().getLatitude()));
      position.setLongitude(new BigDecimal(input.getPosition().getLongitude()));

      resource.setPosition(position);
    }
  }

  @Override
  void setDescription(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    resource.setDescription(input.getDescription());
  }

  @Override
  void setStatus(ca.uhn.fhir.model.dstu2.resource.Location resource, Location input) {
    if (null != input.getStatus()) {
      resource.setStatus(LocationStatusEnum.forCode(input.getStatus()));
    }
  }

}

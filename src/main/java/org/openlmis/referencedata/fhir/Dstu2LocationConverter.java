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
import ca.uhn.fhir.model.dstu2.resource.Location.Position;
import ca.uhn.fhir.model.dstu2.valueset.LocationStatusEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class Dstu2LocationConverter extends LocationConverter<ca.uhn.fhir.model.dstu2.resource.Location> {

  @Override
  public ca.uhn.fhir.model.dstu2.resource.Location apply(Location input) {
    ca.uhn.fhir.model.dstu2.resource.Location location =
        new ca.uhn.fhir.model.dstu2.resource.Location();

    location.setId(input.getId().toString());
    location.setName(input.getName());
    location.setPhysicalType(getPhysicalType(input));
    location.setPartOf(new ResourceReferenceDt(input.getPartOf().getReference()));
    location.setIdentifier(getIdentifiers(input));

    Optional
        .ofNullable(input.getPosition())
        .ifPresent(position -> location.setPosition(getPosition(position)));

    Optional
        .ofNullable(input.getDescription())
        .ifPresent(location::setDescription);

    Optional
        .ofNullable(input.getStatus())
        .ifPresent(status -> location.setStatus(getStatus(status)));

    return location;
  }

  @Override
  void addSystemIdentifier(ca.uhn.fhir.model.dstu2.resource.Location resource,
      String system, UUID value) {
    resource.addIdentifier(new IdentifierDt(system, value.toString()));
  }

  private Position getPosition(org.openlmis.referencedata.fhir.Position inputPosition) {
    Position position = new Position();
    position.setLatitude(inputPosition.getLatitude());
    position.setLongitude(inputPosition.getLongitude());

    return position;
  }

  private CodeableConceptDt getPhysicalType(Location input) {
    CodeableConceptDt physicalType = new CodeableConceptDt();
    input
        .getPhysicalType()
        .getCoding()
        .stream()
        .map(elem -> {
          CodingDt coding = new CodingDt(elem.getSystem(), elem.getCode());
          coding.setDisplay(elem.getDisplay());

          return coding;
        })
        .forEach(physicalType::addCoding);

    return physicalType;
  }

  private LocationStatusEnum getStatus(String inputStatus) {
    return LocationStatusEnum.forCode(inputStatus);
  }

  private List<IdentifierDt> getIdentifiers(Location input) {
    return input
        .getIdentifier()
        .stream()
        .map(elem -> new IdentifierDt(elem.getSystem(), elem.getValue()))
        .collect(Collectors.toList());
  }

}

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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Location.LocationPositionComponent;
import org.hl7.fhir.dstu3.model.Location.LocationStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;

class Dstu3LocationConverter extends LocationConverter<Location> {

  @Override
  Location createResource(FhirLocation input) {
    return new Location();
  }

  @Override
  void setName(Location resource, FhirLocation input) {
    resource.setName(input.getName());
  }

  @Override
  void setPhysicalType(Location resource, FhirLocation input) {
    CodeableConcept physicalType = new CodeableConcept();
    input
        .getPhysicalType()
        .getCoding()
        .stream()
        .map(elem -> {
          Coding coding = new Coding();
          coding.setSystem(elem.getSystem());
          coding.setCode(elem.getCode());
          coding.setDisplay(elem.getDisplay());

          return coding;
        })
        .forEach(physicalType::addCoding);

    resource.setPhysicalType(physicalType);
  }

  @Override
  void setPartOf(Location resource, FhirLocation input) {
    resource.setPartOf(new Reference(input.getPartOf().getReference()));
  }

  @Override
  void setIdentifier(Location resource, FhirLocation input) {
    input
        .getIdentifier()
        .stream()
        .map(elem -> {
          Identifier identifier = new Identifier();
          identifier.setSystem(elem.getSystem());
          identifier.setValue(elem.getValue());

          return identifier;
        })
        .forEach(resource::addIdentifier);
  }

  @Override
  void addSystemIdentifier(Location resource, String system, UUID value) {
    Identifier identifier = new Identifier();
    identifier.setSystem(system);
    identifier.setValue(value.toString());

    resource.addIdentifier(identifier);
  }

  @Override
  void setAlias(Location resource, FhirLocation input) {
    input
        .getAlias()
        .forEach(resource::addAlias);
  }

  @Override
  void setPosition(Location resource, FhirLocation input) {
    Optional
        .ofNullable(input.getPosition())
        .ifPresent(position -> {
          LocationPositionComponent positionComponent = new LocationPositionComponent();
          positionComponent.setLatitude(new BigDecimal(position.getLatitude()));
          positionComponent.setLongitude(new BigDecimal(position.getLongitude()));

          resource.setPosition(positionComponent);
        });
  }

  @Override
  void setDescription(Location resource, FhirLocation input) {
    resource.setDescription(input.getDescription());
  }

  @Override
  void setStatus(Location resource, FhirLocation input) {
    Optional
        .ofNullable(input.getStatus())
        .ifPresent(status -> {
          try {
            resource.setStatus(LocationStatus.fromCode(status));
          } catch (FHIRException exp) {
            throw new IllegalStateException(exp);
          }
        });
  }

}

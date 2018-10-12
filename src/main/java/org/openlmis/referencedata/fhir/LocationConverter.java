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
import java.util.UUID;
import org.hl7.fhir.instance.model.api.IBaseResource;

abstract class LocationConverter<T extends IBaseResource> implements Converter<FhirLocation, T> {

  private String serviceUrl;

  static LocationConverter getInstance(FhirVersionEnum version) {
    if (version == FhirVersionEnum.DSTU3) {
      return new Dstu3LocationConverter();
    }

    throw new IllegalStateException("Unsupported FHIR version: " + version.name());
  }

  @Override
  public T convert(FhirLocation input) {
    T resource = createResource(input);
    setName(resource, input);
    setPhysicalType(resource, input);
    setPartOf(resource, input);
    setIdentifier(resource, input);
    addSystemIdentifier(resource, serviceUrl, input.getId());
    setAlias(resource, input);
    setPosition(resource, input);
    setDescription(resource, input);
    setStatus(resource, input);

    return resource;
  }

  LocationConverter<T> withServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  abstract T createResource(FhirLocation input);

  abstract void setName(T resource, FhirLocation input);

  abstract void setPhysicalType(T resource, FhirLocation input);

  abstract void setPartOf(T resource, FhirLocation input);

  abstract void setIdentifier(T resource, FhirLocation input);

  abstract void addSystemIdentifier(T resource, String system, UUID value);

  abstract void setAlias(T resource, FhirLocation input);

  abstract void setPosition(T resource, FhirLocation input);

  abstract void setDescription(T resource, FhirLocation input);

  abstract void setStatus(T resource, FhirLocation input);

}

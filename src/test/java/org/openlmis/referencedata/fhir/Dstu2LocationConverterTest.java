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

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.referencedata.fhir.Coding.SITE;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import java.util.UUID;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;

public class Dstu2LocationConverterTest {

  private static final String SERVICE_URL = "http://localhost";
  private Dstu2LocationConverter converter = new Dstu2LocationConverter();

  @Before
  public void setUp() {
    converter.withServiceUrl(SERVICE_URL);
  }

  @Test
  public void shouldCovertOpenLmisLocationToFhirLocation() {
    Location olmisLocation = new Location(SERVICE_URL, new FacilityDataBuilder().build());
    ca.uhn.fhir.model.dstu2.resource.Location fhirLocation = converter.convert(olmisLocation);

    assertThat(fhirLocation.getId().toString()).isEqualTo(olmisLocation.getId().toString());
    assertThat(fhirLocation.getName()).isEqualTo(olmisLocation.getName());

    assertThat(fhirLocation.getPhysicalType()).isNotNull();
    assertThat(fhirLocation.getPhysicalType().getCoding()).hasSize(1);
    assertThat(fhirLocation.getPhysicalType().getCoding().get(0))
        .hasFieldOrPropertyWithValue("system", SITE.getSystem())
        .hasFieldOrPropertyWithValue("code", SITE.getCode())
        .hasFieldOrPropertyWithValue("display", SITE.getDisplay());

    assertThat(fhirLocation.getPartOf()).isNotNull();
    assertThat(fhirLocation.getPartOf().getReference().toString())
        .isEqualTo(olmisLocation.getPartOf().getReference());

    assertThat(fhirLocation.getIdentifier()).hasSize(olmisLocation.getIdentifier().size() + 1);
    olmisLocation
        .getIdentifier()
        .forEach(identifier -> assertThat(fhirLocation.getIdentifier())
            .haveExactly(1, new Condition<IdentifierDt>() {
              @Override
              public boolean matches(IdentifierDt value) {
                return value.getSystem().equals(identifier.getSystem())
                    && value.getValue().equals(identifier.getValue());
              }
            }));

    assertThat(fhirLocation.getPosition()).isNotNull();
    assertThat(fhirLocation.getPosition().getLatitude().doubleValue())
        .isEqualTo(olmisLocation.getPosition().getLatitude());
    assertThat(fhirLocation.getPosition().getLongitude().doubleValue())
        .isEqualTo(olmisLocation.getPosition().getLongitude());

    assertThat(fhirLocation.getDescription()).isEqualTo(olmisLocation.getDescription());
    assertThat(fhirLocation.getStatus()).isEqualTo(olmisLocation.getStatus());
  }

  @Test
  public void shouldAddSystemIdentifier() {
    UUID id = UUID.randomUUID();

    ca.uhn.fhir.model.dstu2.resource.Location resource =
        new ca.uhn.fhir.model.dstu2.resource.Location();
    converter.addSystemIdentifier(resource, SERVICE_URL, id);

    assertThat(resource.getIdentifier()).hasSize(1);
    assertThat(resource.getIdentifier().get(0))
        .hasFieldOrPropertyWithValue("system", SERVICE_URL)
        .hasFieldOrPropertyWithValue("value", id.toString());
  }

}

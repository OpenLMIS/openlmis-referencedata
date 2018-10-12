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
import static org.openlmis.referencedata.fhir.FhirCoding.AREA;
import static org.openlmis.referencedata.fhir.FhirCoding.SITE;

import java.util.Optional;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.web.FacilityOperatorController;
import org.openlmis.referencedata.web.FacilityTypeController;
import org.openlmis.referencedata.web.GeographicLevelController;
import org.openlmis.referencedata.web.LocationController;
import org.openlmis.referencedata.web.ProgramController;

public class FhirLocationTest {

  private static final String SERVICE_URL = "http://localhost";

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(FhirLocation.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    FhirLocation dto = FhirLocation.newInstance("service-url", new FacilityDataBuilder().build());
    ToStringTestUtils.verify(FhirLocation.class, dto, "LOCATION");
  }

  @Test
  public void shouldCreateInstanceFromGeographicZone() {
    GeographicZone zone = new GeographicZoneDataBuilder()
        .withParent(new GeographicZoneDataBuilder().build())
        .build();
    FhirLocation fhirLocation = FhirLocation.newInstance(SERVICE_URL, zone);

    assertThat(fhirLocation.getId())
        .isEqualTo(zone.getId());
    assertThat(fhirLocation.getResourceType())
        .isEqualTo(FhirLocation.RESOURCE_TYPE_NAME);
    assertThat(fhirLocation.getAlias())
        .hasSize(1)
        .contains(zone.getCode());
    assertThat(fhirLocation.getIdentifier())
        .hasSize(1)
        .contains(new FhirIdentifier(SERVICE_URL,
            GeographicLevelController.RESOURCE_PATH, zone.getLevel().getId()));
    assertThat(fhirLocation.getName())
        .isEqualTo(zone.getName());
    assertThat(fhirLocation.getPosition())
        .isEqualTo(new FhirPosition(zone.getLongitude(), zone.getLatitude()));
    assertThat(fhirLocation.getPhysicalType())
        .isEqualTo(new FhirPhysicalType(AREA));
    assertThat(fhirLocation.getPartOf())
        .isEqualTo(new FhirReference(SERVICE_URL,
            LocationController.RESOURCE_PATH, zone.getParent().getId()));
    assertThat(fhirLocation.getDescription())
        .isNull();
    assertThat(fhirLocation.getStatus())
        .isNull();
  }

  @Test
  public void shouldCreateInstanceFromFacility() {
    testCreateInstanceFromFacility(new ProgramDataBuilder().build());
  }

  @Test
  public void shouldCreateInstanceFromFacilityWithoutSupportedPrograms() {
    testCreateInstanceFromFacility(null);
  }

  private void testCreateInstanceFromFacility(Program program) {
    FacilityDataBuilder builder = new FacilityDataBuilder();
    Optional.ofNullable(program).ifPresent(builder::withSupportedProgram);
    Facility facility = builder.build();

    FhirLocation fhirLocation = FhirLocation.newInstance(SERVICE_URL, facility);

    assertThat(fhirLocation.getId())
        .isEqualTo(facility.getId());
    assertThat(fhirLocation.getResourceType())
        .isEqualTo(FhirLocation.RESOURCE_TYPE_NAME);
    assertThat(fhirLocation.getAlias())
        .hasSize(1)
        .contains(facility.getCode());
    assertThat(fhirLocation.getIdentifier())
        .hasSize(null == program ? 2 : 3)
        .contains(new FhirIdentifier(SERVICE_URL,
            FacilityTypeController.RESOURCE_PATH, facility.getType().getId()))
        .contains(new FhirIdentifier(SERVICE_URL,
            FacilityOperatorController.RESOURCE_PATH, facility.getOperator().getId()));

    if (null != program) {
      assertThat(fhirLocation.getIdentifier())
          .contains(new FhirIdentifier(SERVICE_URL,
              ProgramController.RESOURCE_PATH, program.getId()));
    }

    assertThat(fhirLocation.getName())
        .isEqualTo(facility.getName());
    assertThat(fhirLocation.getPosition())
        .isEqualTo(new FhirPosition(facility.getLocation().getX(), facility.getLocation().getY()));
    assertThat(fhirLocation.getPhysicalType())
        .isEqualTo(new FhirPhysicalType(SITE));
    assertThat(fhirLocation.getPartOf())
        .isEqualTo(new FhirReference(SERVICE_URL,
            LocationController.RESOURCE_PATH, facility.getGeographicZone().getId()));
    assertThat(fhirLocation.getDescription())
        .isEqualTo(facility.getDescription());
    assertThat(fhirLocation.getStatus())
        .isEqualTo(Status.ACTIVE.toString());
  }

}

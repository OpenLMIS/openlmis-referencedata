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
import static org.openlmis.referencedata.fhir.Coding.AREA;
import static org.openlmis.referencedata.fhir.Coding.SITE;

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

public class LocationTest {

  private static final String SERVICE_URL = "http://localhost";

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(Location.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    Location dto = Location.newInstance("service-url", new FacilityDataBuilder().build());
    ToStringTestUtils.verify(Location.class, dto, "LOCATION");
  }

  @Test
  public void shouldCreateInstanceFromGeographicZone() {
    GeographicZone zone = new GeographicZoneDataBuilder()
        .withParent(new GeographicZoneDataBuilder().build())
        .build();
    Location location = Location.newInstance(SERVICE_URL, zone);

    assertThat(location.getId())
        .isEqualTo(zone.getId());
    assertThat(location.getResourceType())
        .isEqualTo(Location.RESOURCE_TYPE_NAME);
    assertThat(location.getAlias())
        .hasSize(1)
        .contains(zone.getCode());
    assertThat(location.getIdentifier())
        .hasSize(1)
        .contains(new Identifier(SERVICE_URL,
            GeographicLevelController.RESOURCE_PATH, zone.getLevel().getId()));
    assertThat(location.getName())
        .isEqualTo(zone.getName());
    assertThat(location.getPosition())
        .isEqualTo(new Position(zone.getLongitude(), zone.getLatitude()));
    assertThat(location.getPhysicalType())
        .isEqualTo(new PhysicalType(AREA));
    assertThat(location.getPartOf())
        .isEqualTo(new Reference(SERVICE_URL,
            LocationController.RESOURCE_PATH, zone.getParent().getId()));
    assertThat(location.getDescription())
        .isNull();
    assertThat(location.getStatus())
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

    Location location = Location.newInstance(SERVICE_URL, facility);

    assertThat(location.getId())
        .isEqualTo(facility.getId());
    assertThat(location.getResourceType())
        .isEqualTo(Location.RESOURCE_TYPE_NAME);
    assertThat(location.getAlias())
        .hasSize(1)
        .contains(facility.getCode());
    assertThat(location.getIdentifier())
        .hasSize(null == program ? 2 : 3)
        .contains(new Identifier(SERVICE_URL,
            FacilityTypeController.RESOURCE_PATH, facility.getType().getId()))
        .contains(new Identifier(SERVICE_URL,
            FacilityOperatorController.RESOURCE_PATH, facility.getOperator().getId()));

    if (null != program) {
      assertThat(location.getIdentifier())
          .contains(new Identifier(SERVICE_URL,
              ProgramController.RESOURCE_PATH, program.getId()));
    }

    assertThat(location.getName())
        .isEqualTo(facility.getName());
    assertThat(location.getPosition())
        .isEqualTo(new Position(facility.getLocation().getX(), facility.getLocation().getY()));
    assertThat(location.getPhysicalType())
        .isEqualTo(new PhysicalType(SITE));
    assertThat(location.getPartOf())
        .isEqualTo(new Reference(SERVICE_URL,
            LocationController.RESOURCE_PATH, facility.getGeographicZone().getId()));
    assertThat(location.getDescription())
        .isEqualTo(facility.getDescription());
    assertThat(location.getStatus())
        .isEqualTo(Status.ACTIVE.toString());
  }

}

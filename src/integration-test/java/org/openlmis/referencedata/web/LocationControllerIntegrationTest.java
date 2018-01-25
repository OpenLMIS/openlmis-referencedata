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

package org.openlmis.referencedata.web;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableList;
import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.web.fhir.Coding;
import org.openlmis.referencedata.web.fhir.Identifier;
import org.openlmis.referencedata.web.fhir.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.UUID;

public class LocationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/Location";
  private static final String LOCATION = "Location";

  @Value("${service.url}")
  private String serviceUrl;

  private UUID supportedProgramId = UUID.randomUUID();

  @Test
  public void shouldReturnGeographicZoneAsLocation() {
    GeographicZone zone = new GeographicZoneDataBuilder()
        .withParent(new GeographicZoneDataBuilder().build())
        .build();
    given(geographicZoneRepository.findAll(pageable))
        .willReturn(new PageImpl<>(ImmutableList.of(zone)));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200);

    assertLocation(response, zone);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnFacilityAsLocation() {
    Facility facility = new FacilityDataBuilder()
        .withSupportedProgram(new ProgramDataBuilder().withId(supportedProgramId).build())
        .buildActive();
    given(facilityRepository.findAll(pageable))
        .willReturn(new PageImpl<>(ImmutableList.of(facility)));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200);

    assertLocation(response, facility);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void assertLocation(ValidatableResponse response, GeographicZone zone) {
    response
        .body("", hasSize(1))
        .body("[0].resourceType", is(LOCATION))
        .body("[0].id", is(zone.getId().toString()))
        .body("[0].alias", hasSize(1))
        .body("[0].alias[0]", is(zone.getCode()))
        .body("[0].identifier", hasSize(1))
        .body("[0].identifier[0].system", is(Identifier.SYSTEM_RFC_3986))
        .body("[0].identifier[0].value", containsString(zone.getLevel().getId().toString()))
        .body("[0].name", is(zone.getName()))
        .body("[0].position.longitude.doubleValue()", closeTo(zone.getLongitude(),  0.1))
        .body("[0].position.latitude.doubleValue()", closeTo(zone.getLatitude(), 0.1))
        .body("[0].physicalType.coding", hasSize(1))
        .body("[0].physicalType.coding[0].system", is(Coding.AREA.getSystem()))
        .body("[0].physicalType.coding[0].code", is(Coding.AREA.getCode()))
        .body("[0].physicalType.coding[0].display", is(Coding.AREA.getDisplay()));
  }

  private void assertLocation(ValidatableResponse response, Facility facility) {
    response
        .body("", hasSize(1))
        .body("[0].resourceType", is(LOCATION))
        .body("[0].id", is(facility.getId().toString()))
        .body("[0].alias", hasSize(1))
        .body("[0].alias[0]", is(facility.getCode()))
        .body("[0].identifier", hasSize(3))
        .body("[0].identifier[0].system", is(Identifier.SYSTEM_RFC_3986))
        .body("[0].identifier[1].system", is(Identifier.SYSTEM_RFC_3986))
        .body("[0].identifier[2].system", is(Identifier.SYSTEM_RFC_3986))
        .body("[0].identifier[0].value",
            is(serviceUrl
                + ProgramController.RESOURCE_PATH + '/' + supportedProgramId))
        .body("[0].identifier[1].value",
            is(serviceUrl
                + FacilityTypeController.RESOURCE_PATH + '/' + facility.getType().getId()))
        .body("[0].identifier[2].value",
            is(serviceUrl
                + FacilityOperatorController.RESOURCE_PATH + '/' + facility.getOperator().getId()))
        .body("[0].name", is(facility.getName()))
        .body("[0].description", is(facility.getDescription()))
        .body("[0].status", is(Status.ACTIVE.toString()))
        .body("[0].position.longitude.doubleValue()", closeTo(facility.getLocation().getX(), 0.1))
        .body("[0].position.latitude.doubleValue()", closeTo(facility.getLocation().getY(), 0.1))
        .body("[0].physicalType.coding", hasSize(1))
        .body("[0].physicalType.coding[0].system", is(Coding.SITE.getSystem()))
        .body("[0].physicalType.coding[0].code", is(Coding.SITE.getCode()))
        .body("[0].physicalType.coding[0].display", is(Coding.SITE.getDisplay()));
  }

}

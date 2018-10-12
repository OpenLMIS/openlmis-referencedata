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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.openlmis.referencedata.web.BaseController.API_PATH;

import com.google.common.collect.ImmutableList;
import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.fhir.FhirCoding;
import org.openlmis.referencedata.fhir.FhirIdentifier;
import org.openlmis.referencedata.fhir.Status;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    ValidatableResponse response = getLocations();

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

    ValidatableResponse response = getLocations();

    assertLocation(response, facility);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse getLocations() {
    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body("", hasSize(1))
        .body("[0].resourceType", is(LOCATION));
  }

  private void assertLocation(ValidatableResponse response, GeographicZone zone) {
    response
        .body("[0].id", is(zone.getId().toString()))
        .body("[0].alias", hasSize(1))
        .body("[0].alias[0]", is(zone.getCode()))
        .body("[0].identifier", hasSize(1))
        .body("[0].name", is(zone.getName()));

    checkIdentifier(response, 0,
        GeographicLevelController.RESOURCE_PATH + '/' + zone.getLevel().getId());
    checkPartOf(response, LocationController.RESOURCE_PATH + '/' + zone.getParent().getId());
    checkPosition(response, zone.getLongitude(), zone.getLatitude());
    checkPhysicalType(response, FhirCoding.AREA);
  }

  private void assertLocation(ValidatableResponse response, Facility facility) {
    response
        .body("[0].id", is(facility.getId().toString()))
        .body("[0].alias", hasSize(1))
        .body("[0].alias[0]", is(facility.getCode()))
        .body("[0].identifier", hasSize(3))
        .body("[0].name", is(facility.getName()))
        .body("[0].description", is(facility.getDescription()))
        .body("[0].status", is(Status.ACTIVE.toString()));

    checkIdentifier(response, 0,
        ProgramController.RESOURCE_PATH + '/' + supportedProgramId);
    checkIdentifier(response, 1,
        FacilityTypeController.RESOURCE_PATH + '/' + facility.getType().getId());
    checkIdentifier(response, 2,
        FacilityOperatorController.RESOURCE_PATH + '/' + facility.getOperator().getId());
    checkPartOf(response,
        LocationController.RESOURCE_PATH + '/' + facility.getGeographicZone().getId());
    checkPosition(response, facility.getLocation().getX(), facility.getLocation().getY());
    checkPhysicalType(response, FhirCoding.SITE);
  }

  private void checkPartOf(ValidatableResponse response, String path) {
    response
        .body("[0].partOf.reference", is(serviceUrl + API_PATH + path));
  }

  private void checkIdentifier(ValidatableResponse response, int idx, String path) {
    response
        .body("[0].identifier[" + idx + "].system", is(FhirIdentifier.SYSTEM_RFC_3986))
        .body("[0].identifier[" + idx + "].value", is(serviceUrl + path));
  }

  private void checkPosition(ValidatableResponse response, double longitude, double latitude) {
    response
        .body("[0].position.longitude.doubleValue()", closeTo(longitude, 0.1))
        .body("[0].position.latitude.doubleValue()", closeTo(latitude, 0.1));
  }


  private void checkPhysicalType(ValidatableResponse response, FhirCoding coding) {
    response
        .body("[0].physicalType.coding", hasSize(1))
        .body("[0].physicalType.coding[0].system", is(coding.getSystem()))
        .body("[0].physicalType.coding[0].code", is(coding.getCode()))
        .body("[0].physicalType.coding[0].display", is(coding.getDisplay()));
  }

}

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

import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.web.fhir.Coding;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

public class LocationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/Location";

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

  private void assertLocation(ValidatableResponse response, GeographicZone zone) {
    response
        .body("", hasSize(1))
        .body("[0].resourceType", is("Location"))
        .body("[0].id", is(zone.getId().toString()))
        .body("[0].alias", hasSize(1))
        .body("[0].alias[0]", is(zone.getCode()))
        .body("[0].identifier", hasSize(1))
        .body("[0].identifier[0].value", containsString(zone.getLevel().getId().toString()))
        .body("[0].name", is(zone.getName()))
        .body("[0].position.longitude.doubleValue()", closeTo(zone.getLongitude(),  0.1))
        .body("[0].position.latitude.doubleValue()", closeTo(zone.getLatitude(), 0.1))
        .body("[0].physicalType.coding", hasSize(1))
        .body("[0].physicalType.coding[0].system", is(Coding.AREA.getSystem()))
        .body("[0].physicalType.coding[0].code", is(Coding.AREA.getCode()))
        .body("[0].physicalType.coding[0].display", is(Coding.AREA.getDisplay()));
  }

}

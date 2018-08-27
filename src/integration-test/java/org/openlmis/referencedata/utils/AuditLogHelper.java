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

package org.openlmis.referencedata.utils;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public final class AuditLogHelper {

  private AuditLogHelper() {
    throw new UnsupportedOperationException();
  }

  public static void notFound(RestAssuredClient client, String token, String resourceUrl) {
    withStatus(client, token, resourceUrl, 404);
  }

  public static void unauthorized(RestAssuredClient client, String token, String resourceUrl) {
    withStatus(client, token, resourceUrl, 403);
  }

  public static void ok(RestAssuredClient client, String token, String resourceUrl) {
    withStatus(client, token, resourceUrl, 200);
  }

  private static void withStatus(RestAssuredClient client, String token, String resourceUrl,
                                 int statusCode) {
    get(client, token, resourceUrl).then().statusCode(statusCode);
  }

  private static Response get(RestAssuredClient client, String token, String resourceUrl) {
    return client
        .given()
        .header(HttpHeaders.AUTHORIZATION, token)
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(getAuditPath(resourceUrl));
  }

  private static String getAuditPath(String resourceUrl) {
    return resourceUrl + "/{id}/auditLog";
  }

}

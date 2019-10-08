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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openlmis.referencedata.web.OrderableFulfillSearchParams.FACILITY_ID;
import static org.openlmis.referencedata.web.OrderableFulfillSearchParams.ID;
import static org.openlmis.referencedata.web.OrderableFulfillSearchParams.PROGRAM_ID;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.springframework.util.LinkedMultiValueMap;

public class OrderableFulfillSearchParamsTest {

  private static final String VALUE = "test";
  private static final UUID UUID_VALUE = UUID.randomUUID();
  private static final UUID UUID_VALUE_2 = UUID.randomUUID();

  @Test
  public void getFacilityTypeIdShouldReturnValueForKeyFacilityId() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("facilityId", UUID_VALUE.toString());
    queryMap.add(PROGRAM_ID, UUID_VALUE_2.toString());
    OrderableFulfillSearchParams searchParams = new OrderableFulfillSearchParams(queryMap);

    assertEquals(UUID_VALUE, searchParams.getFacilityId());
  }

  @Test
  public void getFacilityIdReturnNullIfValueForRequestParamIsNotProvided() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    OrderableFulfillSearchParams searchParams = new OrderableFulfillSearchParams(queryMap);

    assertNull(null, searchParams.getFacilityId());
  }

  @Test
  public void getProgramIdShouldReturnValueForKeyProgramId() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(FACILITY_ID, UUID_VALUE.toString());
    queryMap.add("programId", UUID_VALUE_2.toString());
    OrderableFulfillSearchParams searchParams = new OrderableFulfillSearchParams(queryMap);

    assertEquals(UUID_VALUE_2, searchParams.getProgramId());
  }

  @Test
  public void getProgramIdReturnNullIfValueForRequestParamIsNotProvided() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    OrderableFulfillSearchParams searchParams = new OrderableFulfillSearchParams(queryMap);

    assertNull(null, searchParams.getProgramId());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenConstructingWithUnknownProperty() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add("unknownPropertyName", VALUE);
    new OrderableFulfillSearchParams(queryMap);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenConstructingWithProgramIdAndWithoutFacilityId() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, UUID_VALUE_2.toString());
    new OrderableFulfillSearchParams(queryMap);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenConstructingWithFacilityIdAndWithoutProgramId() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(FACILITY_ID, UUID_VALUE.toString());
    new OrderableFulfillSearchParams(queryMap);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowWhenConstructingWithIdFacilityIdAndProgramId() {
    LinkedMultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(ID, UUID_VALUE.toString());
    queryMap.add(PROGRAM_ID, UUID_VALUE.toString());
    queryMap.add(FACILITY_ID, UUID_VALUE.toString());
    new OrderableFulfillSearchParams(queryMap);
  }
}

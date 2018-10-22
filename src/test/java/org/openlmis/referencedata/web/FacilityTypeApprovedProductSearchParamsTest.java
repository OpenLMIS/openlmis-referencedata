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

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys.ERROR_LACK_PARAMS;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.springframework.util.LinkedMultiValueMap;

public class FacilityTypeApprovedProductSearchParamsTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private static final String FACILITY_TYPE = "facilityType";
  private static final String PROGRAM = "program";

  private LinkedMultiValueMap<String, Object> queryMap;

  @Before
  public void setUp() {
    queryMap = new LinkedMultiValueMap<>();
  }

  @Test
  public void shouldGetFacilityTypeValuesFromParameters() {
    queryMap.add(FACILITY_TYPE, "facilityType");
    FacilityTypeApprovedProductSearchParams params
        = new FacilityTypeApprovedProductSearchParams(queryMap);

    assertThat(params.getFacilityTypeCodes())
        .hasSize(1)
        .contains("facilityType");
  }

  @Test
  public void shouldThrowExceptionIfMapHasNoFacilityTypeProperty() {
    FacilityTypeApprovedProductSearchParams params
        = new FacilityTypeApprovedProductSearchParams(queryMap);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_LACK_PARAMS);

    params.getFacilityTypeCodes();
  }

  @Test
  public void shouldGetProgramValueFromParameters() {
    queryMap.add(PROGRAM, "name");
    FacilityTypeApprovedProductSearchParams params
        = new FacilityTypeApprovedProductSearchParams(queryMap);

    assertThat(params.getProgram()).isEqualTo("name");
  }

  @Test
  public void shouldGetNullIfMapHasNoNameProperty() {
    FacilityTypeApprovedProductSearchParams params
        = new FacilityTypeApprovedProductSearchParams(queryMap);

    assertThat(params.getProgram()).isNull();
  }

  @Test
  public void shouldThrowExceptionIfThereIsUnknownParameterInParameters() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_INVALID_PARAMS);

    queryMap.add("some-param", "some-value");
    new FacilityTypeApprovedProductSearchParams(queryMap);
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(FacilityTypeApprovedProductSearchParams.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    queryMap.add(PROGRAM, "some-name");
    FacilityTypeApprovedProductSearchParams params
        = new FacilityTypeApprovedProductSearchParams(queryMap);

    ToStringTestUtils.verify(FacilityTypeApprovedProductSearchParams.class, params,
        "FACILITY_TYPE", "PROGRAM", "ALL_PARAMETERS");
  }
}

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
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.http.ResponseEntity;

public class DataExportControllerTest {

  private static final int HTTP_OK_STATUS_CODE = 200;
  private static final String DATA_QUERY_PARAM_VALUE = "test-data-value";
  private String queryParamValue;

  @InjectMocks
  private DataExportController controller;

  /**
   * Constructor for test.
   */
  public DataExportControllerTest() {
    initMocks(this);
  }

  @Test
  public void shouldReturnHttp200OkSuccessStatusResponse() {
    //given
    queryParamValue = DATA_QUERY_PARAM_VALUE;

    //when
    ResponseEntity response = controller.exportData(queryParamValue);

    //then
    assertThat(response.getStatusCode().value()).isEqualTo(HTTP_OK_STATUS_CODE);
  }

}
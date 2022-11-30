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

import static org.openlmis.referencedata.web.DataExportController.RESOURCE_PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(RESOURCE_PATH)
public class DataExportController extends BaseController {

  public static final String RESOURCE_PATH = BaseController.API_PATH + "/exportData";
  private static final String DATA_QUERY_PARAM = "data";

  /**
   * Exports the given data to a ZIP with CSV files in OpenLMIS
   * Configuration Data Export File format.
   *
   * @param data The names of the files to be exported.
   */
  @GetMapping
  @ResponseBody
  public ResponseEntity exportData(@RequestParam(value = DATA_QUERY_PARAM) String data) {
    return new ResponseEntity(HttpStatus.OK);
  }

}

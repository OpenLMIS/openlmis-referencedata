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

package org.openlmis.referencedata.web.export;

import static org.openlmis.referencedata.web.export.DataExportController.RESOURCE_PATH;

import java.util.Map;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.service.export.DataExportService;
import org.openlmis.referencedata.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  public static final String ZIP_MEDIA_TYPE = "application/zip";
  private static final String RESPONSE_FILE_NAME = "OLMIS_configuration_data.zip";

  @Autowired
  private DataExportService dataExportService;


  /**
   * Exports the given data to a ZIP with CSV files in OpenLMIS
   * Configuration Data Export File format.
   *
   * @param requestParams Required parameters: format (output format for files) and date
   *                      (names of requested files).
   * @return Zip archive bytes containing formatted files
   */
  @GetMapping
  @ResponseBody
  public ResponseEntity<byte[]> exportData(@RequestParam Map<String, String> requestParams) {
    rightService.checkAdminRight(RightName.DATA_EXPORT);

    return ResponseEntity.ok()
            .contentType(MediaType.valueOf(ZIP_MEDIA_TYPE))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + RESPONSE_FILE_NAME)
            .body(dataExportService.exportData(new DataExportParams(requestParams)));
  }

}
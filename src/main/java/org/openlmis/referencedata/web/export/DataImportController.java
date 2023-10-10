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

import static org.openlmis.referencedata.web.export.DataImportController.RESOURCE_PATH;

import java.util.List;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.BaseDto;
import org.openlmis.referencedata.service.export.DataImportService;
import org.openlmis.referencedata.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(RESOURCE_PATH)
public class DataImportController extends BaseController {

  public static final String RESOURCE_PATH = BaseController.API_PATH + "/importData";

  @Autowired
  private DataImportService dataImportService;

  /**
   * Imports the data from a ZIP with CSV files.
   *
   * @param file ZIP archive being imported.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<BaseDto>> importData(@RequestPart("file") MultipartFile file) {
    rightService.checkAdminRight(RightName.DATA_IMPORT);
    List<BaseDto> importedData = dataImportService.importData(file);
    return ResponseEntity.ok().body(importedData);
  }

}
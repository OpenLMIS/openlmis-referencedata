package org.openlmis.referencedata.web;

import org.springframework.web.bind.annotation.RequestParam;

public class DataExportController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/exportData";

  /**
   * Export data to a file of a given format.
   *
   * @param data   The names of the files to be exported.
   */
  public void exportData(@RequestParam(value = "data", required = true) String data) {

  }

}

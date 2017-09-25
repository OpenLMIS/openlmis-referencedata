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

package org.openlmis.referencedata.web.csv.processor;

import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.model.ModelField;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class has mappings from type to cell processors used for parsing value in a cell
 * to corresponding data type.
 */
public class CsvCellProcessors {

  public static final String FACILITY_TYPE = "Facility";
  public static final String PROGRAM_TYPE = "Program";
  public static final String ORDERABLE_TYPE = "Orderable";
  public static final String PROCESSING_PERIOD_TYPE = "ProcessingPeriod";

  private static final Map<String, CellProcessor> typeExportMappings = new HashMap<>();

  static {
    typeExportMappings.put(FACILITY_TYPE, new FormatFacility());
    typeExportMappings.put(PROGRAM_TYPE, new FormatProgram());
    typeExportMappings.put(ORDERABLE_TYPE, new FormatOrderable());
    typeExportMappings.put(PROCESSING_PERIOD_TYPE, new FormatProcessingPeriod());
  }

  /**
   * Get all format processors for given headers.
   */
  public static List<CellProcessor> getProcessors(ModelClass modelClass,
                                                   List<String> headers) {
    List<CellProcessor> processors = new ArrayList<>();
    for (String header : headers) {
      ModelField field = modelClass.findImportFieldWithName(header);
      CellProcessor processor = null;
      if (field != null) {
        processor = chainTypeProcessor(field);
      }
      processors.add(processor);
    }
    return processors;
  }

  private static CellProcessor chainTypeProcessor(ModelField field) {
    CellProcessor mappedProcessor;
    if (typeExportMappings.containsKey(field.getType())) {
      mappedProcessor = typeExportMappings.get(field.getType());
    } else {
      mappedProcessor = new Trim();
    }

    return field.isMandatory() ? new NotNull(mappedProcessor) : new Optional(mappedProcessor);
  }
}

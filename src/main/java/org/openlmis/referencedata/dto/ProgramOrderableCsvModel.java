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

package org.openlmis.referencedata.dto;

import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.BOOLEAN_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.POSITIVE_INT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.web.csv.model.ImportField;

/**
 * This class represents the data model retrieved from the db for the data export functionality.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramOrderableCsvModel {

  @ImportField(name = "program", mandatory = true)
  private String programCode;

  @ImportField(name = "code", mandatory = true)
  private String orderableCode;

  @ImportField(name = "dosesPerPatient", type = POSITIVE_INT)
  private Integer dosesPerPatient;

  @ImportField(name = "active", type = BOOLEAN_TYPE, mandatory = true)
  private boolean active;

  @ImportField(name = "category", mandatory = true)
  private String categoryCode;

  @ImportField(name = "fullSupply", type = BOOLEAN_TYPE, mandatory = true)
  private boolean fullSupply;

  @ImportField(name = "displayOrder", type = POSITIVE_INT)
  private Integer displayOrder;

  @ImportField(name = "pricePerPack")
  private String pricePerPack;

}

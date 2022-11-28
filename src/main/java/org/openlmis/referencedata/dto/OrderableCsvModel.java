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

import lombok.*;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.web.csv.model.ImportField;

import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.DISPENSABLE_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.POSITIVE_LONG;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderableCsvModel extends BaseDto implements Orderable.CsvExporter {

  private static final String PRODUCT_CODE = "productCode";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String PACK_ROUNDING_THRESHOLD = "packRoundingThreshold";
  private static final String PACK_SIZE = "packSize";
  private static final String ROUND_TO_ZERO = "roundToZero";
  private static final String DISPENSABLE = "dispensable";

  @Getter
  @Setter
  @ImportField(name = PRODUCT_CODE, mandatory = true)
  private String productCode;

  @Getter
  @Setter
  @ImportField(name = NAME, mandatory = true)
  private String fullProductName;

  @Getter
  @Setter
  @ImportField(name = DESCRIPTION, mandatory = true)
  private String description;

  @Getter
  @Setter
  @ImportField(name = PACK_ROUNDING_THRESHOLD, type = POSITIVE_LONG, mandatory = true)
  private Long packRoundingThreshold;

  @Getter
  @Setter
  @ImportField(name = PACK_SIZE, type = POSITIVE_LONG, mandatory = true)
  private Long netContent;

  @Getter
  @Setter
  @ImportField(name = ROUND_TO_ZERO, mandatory = true)
  private Boolean roundToZero;

  @Getter
  @ImportField(name = DISPENSABLE, type = DISPENSABLE_TYPE, mandatory = true)
  private DispensableDto dispensable;

  public void setDispensable(DispensableDto dispensable) {
    this.dispensable = dispensable;
  }

  @Override
  public void setDispensable(Dispensable dispensable) {
    if (dispensable != null) {
      this.dispensable = new DispensableDto();
      dispensable.export(this.dispensable);
    } else {
      this.dispensable = null;
    }
  }
}


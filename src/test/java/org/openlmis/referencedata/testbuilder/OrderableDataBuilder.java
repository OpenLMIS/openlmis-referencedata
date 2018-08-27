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

package org.openlmis.referencedata.testbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.ProgramOrderable;

public class OrderableDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private Code productCode;
  private Dispensable dispensable;
  private String fullProductName;
  private String description;
  private long netContent;
  private long packRoundingTreshold;
  private boolean roundToZero;
  private List<ProgramOrderable> programOrderables;
  private Map<String, String> identifiers;
  private Map<String, String> extraData;

  /**
   * Returns instance of {@link OrderableDataBuilder} with sample data.
   */
  public OrderableDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    productCode = Code.code("P" + instanceNumber);
    dispensable = Dispensable.createNew("pack");
    fullProductName = "product " + instanceNumber;
    description = "description";
    netContent = 10;
    packRoundingTreshold = 5;
    roundToZero = false;
    programOrderables = new ArrayList<>();
    identifiers = new HashMap<>();
  }

  public OrderableDataBuilder withIdentifier(String key, Object valueToString) {
    this.identifiers.put(key, valueToString.toString());
    return this;
  }

  public OrderableDataBuilder withProductCode(Code productCode) {
    this.productCode = productCode;
    return this;
  }

  public OrderableDataBuilder withDispensable(Dispensable dispensable) {
    this.dispensable = dispensable;
    return this;
  }

  public OrderableDataBuilder withFullProductName(String fullProductName) {
    this.fullProductName = fullProductName;
    return this;
  }

  /**
   * Builds instance of {@link Orderable}.
   */
  public Orderable build() {
    Orderable orderable = buildAsNew();
    orderable.setId(id);

    return orderable;
  }

  /**
   * Builds instance of {@link Orderable} without id field.
   */
  public Orderable buildAsNew() {
    return new Orderable(productCode, dispensable, fullProductName, description,
        netContent, packRoundingTreshold, roundToZero, programOrderables, identifiers, extraData);
  }
}

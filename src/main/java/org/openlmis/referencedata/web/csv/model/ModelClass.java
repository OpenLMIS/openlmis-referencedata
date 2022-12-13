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

package org.openlmis.referencedata.web.csv.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a Java model to which the csv row is mapped.
 */
@Data
@NoArgsConstructor
public class ModelClass<T extends Object> {

  private Class<T> clazz;

  private List<ModelField> importFields;

  public ModelClass(Class<T> clazz) {
    this.clazz = clazz;
    importFields = ModelClassHelper.getModelFields(clazz);
  }

  /**
   * Returns array of field paths.
   */
  public String[] getFieldNameMappings(String[] headers) {
    List<String> fieldMappings = new ArrayList<>();

    for (String header : headers) {
      ModelField importField = findImportFieldWithName(header);

      if (importField != null) {
        String nestedProperty = importField.getNested();

        if (nestedProperty.isEmpty()) {
          fieldMappings.add(importField.getField().getName());
        } else {
          fieldMappings.add(importField.getField().getName() + "." + nestedProperty);
        }
      } else {
        fieldMappings.add(null);
      }

    }
    return fieldMappings.toArray(new String[fieldMappings.size()]);
  }

  /**
   * Returns import field with given name.
   *
   * @param name ImportField name
   * @return import field with given name.
   */
  public ModelField findImportFieldWithName(final String name) {
    return importFields
        .stream()
        .filter(field -> field.hasName(name))
        .findFirst()
        .orElse(null);
  }

}

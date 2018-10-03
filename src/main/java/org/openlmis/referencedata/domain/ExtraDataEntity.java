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

package org.openlmis.referencedata.domain;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ExtraDataEntity {

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  @Getter
  private Map<String, String> extraData = new HashMap<>();

  public ExtraDataEntity(Map<String, String> importer) {
    updateFrom(importer);
  }

  static ExtraDataEntity defaultEntity(ExtraDataEntity entity) {
    return entity == null ? new ExtraDataEntity() : entity;
  }

  /**
   * Update this from another.
   */
  public void updateFrom(Map<String, String> importer) {
    extraData.clear();
    Optional
        .ofNullable(importer)
        .ifPresent(extraData::putAll);
  }

  /**
   * Exports current state of object.
   */
  public void export(ExtraDataExporter exporter) {
    Map<String, String> newExtraData = Maps.newHashMap();
    Optional.ofNullable(extraData).ifPresent(newExtraData::putAll);

    exporter.setExtraData(newExtraData);
  }

  public interface ExtraDataExporter {

    void setExtraData(Map<String, String> extraData);

  }

  public interface ExtraDataImporter {

    Map<String, String> getExtraData();

  }

}

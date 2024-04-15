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

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Ward;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WardDto extends BaseDto implements Ward.Exporter, Ward.Importer {

  private FacilityDto facility;
  private String name;
  private String description;
  private boolean disabled;
  private String code;

  /**
   * Creates new instance based on domain object.
   */
  public static WardDto newInstance(Ward ward) {
    WardDto dto = new WardDto();
    ward.export(dto);

    return dto;
  }

  @Override
  public Optional<Facility.Exporter> provideFacilityExporter() {
    return Optional.of(new FacilityDto());
  }

  @Override
  public void includeFacility(Facility.Exporter facilityExporter) {
    facility = (FacilityDto) facilityExporter;
  }

}
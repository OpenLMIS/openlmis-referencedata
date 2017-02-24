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

package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class GeographicZoneService {

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  /**
   * Retrieves recursively all geographic zones that are descendants of the given one.
   *
   * @param root root of zone hierarchy
   * @return collection with all descendant zones.
   */
  public Collection<GeographicZone> getAllZonesInHierarchy(GeographicZone root) {
    Collection<GeographicZone> children = geographicZoneRepository.findByParentAndLevel(root, null);
    Collection<GeographicZone> result = new ArrayList<>(children);

    for (GeographicZone zone : children) {
      Collection<GeographicZone> descendants = getAllZonesInHierarchy(zone);
      result.addAll(descendants);
    }

    return result;
  }
}

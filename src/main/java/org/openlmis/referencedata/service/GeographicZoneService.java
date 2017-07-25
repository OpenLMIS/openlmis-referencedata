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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.UuidUtil;
import org.openlmis.referencedata.util.messagekeys.GeographicLevelMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GeographicZoneService {

  static final String NAME = "name";
  static final String CODE = "code";
  static final String PARENT = "parent";
  static final String LEVEL_NUMBER = "levelNumber";

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  /**
   * Method returns page of geographic zones with matched parameters
   * and sorts results by geographic zone name.
   *
   * @param queryMap request parameters (code, name, parent, levelNumber).
   * @return Page of geographic zones.
   */
  public Page<GeographicZone> search(Map<String, Object> queryMap,
                                                      Pageable pageable) {
    if (MapUtils.isEmpty(queryMap)) {
      return geographicZoneRepository.findAllByOrderByNameAsc(pageable);
    }

    String name = MapUtils.getString(queryMap, NAME, null);
    String code = MapUtils.getString(queryMap, CODE, null);
    String levelNumber = MapUtils.getString(queryMap, LEVEL_NUMBER, null);
    Optional<UUID> parentId = UuidUtil
        .fromString(MapUtils.getObject(queryMap, PARENT, "").toString());

    if (StringUtils.isEmpty(code)
        && StringUtils.isEmpty(name)
        && !parentId.isPresent()
        && StringUtils.isEmpty(levelNumber)) {

      throw new ValidationMessageException(
          GeographicZoneMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
    }

    GeographicZone parent = findGeographicZone(parentId);
    GeographicLevel level = findGeographicLevel(levelNumber);

    return geographicZoneRepository.search(name, code, parent, level, pageable);
  }

  /**
   * Retrieves recursively all geographic zones that are descendants of the given one.
   *
   * @param root root of zone hierarchy
   * @return collection with all descendant zones.
   */
  public Collection<GeographicZone> getAllZonesInHierarchy(GeographicZone root) {
    Collection<GeographicZone> children = geographicZoneRepository.findByParent(root);
    Collection<GeographicZone> result = new ArrayList<>(children);

    for (GeographicZone zone : children) {
      Collection<GeographicZone> descendants = getAllZonesInHierarchy(zone);
      result.addAll(descendants);
    }

    return result;
  }

  private GeographicZone findGeographicZone(Optional<UUID> parentId) {
    GeographicZone parent = null;
    if (parentId.isPresent()) {
      parent = geographicZoneRepository.findOne(parentId.get());
      if (parent == null) {
        throw new ValidationMessageException(
            new Message(GeographicZoneMessageKeys.ERROR_NOT_FOUND_WITH_ID, parentId));
      }
    }
    return parent;
  }

  private GeographicLevel findGeographicLevel(String levelNumber) {
    GeographicLevel level = null;
    if (!StringUtils.isEmpty(levelNumber)) {
      level = geographicLevelRepository.findByLevelNumber(Integer.parseInt(levelNumber));
      if (level == null) {
        throw new ValidationMessageException(
            new Message(GeographicLevelMessageKeys.ERROR_NOT_FOUND_WITH_NUMBER, levelNumber));
      }
    }
    return level;
  }
}

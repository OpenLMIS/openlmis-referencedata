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

package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.util.messagekeys.GeographicLevelMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Controller
@Transactional
public class GeographicLevelController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicLevelController.class);

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  /**
   * Allows creating new geographicLevels.
   *
   * @param geographicLevel A geographicLevel bound to the request body.
   * @return the created geographicLevel.
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public GeographicLevel createGeographicLevel(
      @RequestBody GeographicLevel geographicLevel) {
    LOGGER.debug("Creating new geographicLevel");
    // Ignore provided id
    geographicLevel.setId(null);
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  /**
   * Get all geographicLevels.
   *
   * @return GeographicLevels.
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<GeographicLevel> getAllGeographicLevel() {
    Iterable<GeographicLevel> geographicLevels = geographicLevelRepository.findAll();
    if (geographicLevels == null) {
      throw new NotFoundException(GeographicLevelMessageKeys.ERROR_NOT_FOUND);
    } else {
      return geographicLevels;
    }
  }

  /**
   * Allows updating geographicLevels.
   *
   * @param geographicLevel   A geographicLevel bound to the request body.
   * @param geographicLevelId UUID of geographicLevel which we want to update.
   * @return the updated geographicLevel.
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public GeographicLevel updateGeographicLevel(
      @RequestBody GeographicLevel geographicLevel, @PathVariable("id") UUID geographicLevelId) {
    LOGGER.debug("Updating geographicLevel");
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  /**
   * Get chosen geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to get
   * @return the geographicLevel.
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public GeographicLevel getGeographicLevel(
      @PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      throw new NotFoundException(GeographicLevelMessageKeys.ERROR_NOT_FOUND);
    } else {
      return geographicLevel;
    }
  }

  /**
   * Allows deleting geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to delete
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGeographicLevel(
      @PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      throw new NotFoundException(GeographicLevelMessageKeys.ERROR_NOT_FOUND);
    } else {
      geographicLevelRepository.delete(geographicLevel);
    }
  }
}

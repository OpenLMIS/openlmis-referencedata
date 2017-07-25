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

import com.vividsolutions.jts.geom.Point;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.GeographicZoneDto;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.service.GeographicZoneService;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
public class GeographicZoneController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicZoneController.class);

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicZoneService geographicZoneService;

  /**
   * Allows creating new geographicZones.
   *
   * @param geographicZoneDto A geographicZone bound to the request body.
   * @return the created geographicZone.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public GeographicZoneDto createGeographicZone(
      @RequestBody GeographicZoneDto geographicZoneDto) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

    LOGGER.debug("Creating new geographicZone");
    GeographicZone geographicZone = GeographicZone.newGeographicZone(geographicZoneDto);
    // Ignore provided id
    geographicZone.setId(null);
    return toDto(geographicZoneRepository.save(geographicZone));
  }


  /**
   * Get all geographic zones.
   *
   * @return GeographicZones.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<GeographicZoneSimpleDto> getAllGeographicZones(Pageable pageable) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);
    return (geographicZoneRepository.findAll(pageable)).map(this::toSimpleDto);
  }

  /**
   * Allows updating geographicZones.
   *
   * @param geographicZoneDto A geographicZone bound to the request body.
   * @param geographicZoneId  UUID of geographicZone which we want to update.
   * @return the ResponseEntity containing the updated geographicZone.
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public GeographicZoneDto updateGeographicZone(
      @RequestBody GeographicZoneDto geographicZoneDto, @PathVariable("id") UUID geographicZoneId) {

    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

    GeographicZone geoZoneToSave = GeographicZone.newGeographicZone(geographicZoneDto);
    geoZoneToSave.setId(geographicZoneId);

    LOGGER.debug("Updating geographicZone");
    return toDto(geographicZoneRepository.save(geoZoneToSave));
  }

  /**
   * Get chosen geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to get.
   * @return the geographicZone.
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public GeographicZoneDto getGeographicZone(
      @PathVariable("id") UUID geographicZoneId) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      throw new NotFoundException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    } else {
      return toDto(geographicZone);
    }
  }

  /**
   * Allows deleting geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to delete
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGeographicZone(@PathVariable("id") UUID geographicZoneId) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      throw new NotFoundException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    } else {
      geographicZoneRepository.delete(geographicZone);
    }
  }

  /**
   * Retrieves all geographic zones to which a location belongs.
   *
   * @param location GeoJSON point specifying a location
   * @return List of wanted geographic zones to which the location belongs.
   */
  @RequestMapping(value = "/geographicZones/byLocation", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<GeographicZoneSimpleDto> findGeographicZonesByLocation(
      @RequestBody Point location) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicZone> foundGeoZones = geographicZoneRepository.findByLocation(location);
    return toSimpleDto(foundGeoZones);
  }

  /**
   * Retrieves page of Geographic Zones matching given parameters.
   *
   * @param queryParams request parameters (code, name, parent, levelNumber).
   * @param pageable object used to encapsulate the pagination related values: page and size.
   * @return Page of matched geographic zones.
   */
  @RequestMapping(value = "/geographicZones/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<GeographicZoneSimpleDto> search(@RequestBody Map<String, Object> queryParams,
                                                                 Pageable pageable) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    Page<GeographicZone> page = geographicZoneService.search(queryParams, pageable);

    return exportToDto(page, pageable);
  }

  private Page<GeographicZoneSimpleDto> exportToDto(Page<GeographicZone> page, Pageable pageable) {
    List<GeographicZoneSimpleDto> list = page.getContent().stream()
        .map(this::toSimpleDto).collect(Collectors.toList());
    return Pagination.getPage(list, pageable, page.getTotalElements());
  }

  private GeographicZoneDto toDto(GeographicZone geographicZone) {
    GeographicZoneDto dto = new GeographicZoneDto();
    geographicZone.export(dto);

    return dto;
  }

  private Iterable<GeographicZoneDto> toDto(Iterable<GeographicZone> geographicZones) {
    return StreamSupport
        .stream(geographicZones.spliterator(), false)
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  private GeographicZoneSimpleDto toSimpleDto(GeographicZone geographicZone) {
    GeographicZoneSimpleDto dto = new GeographicZoneSimpleDto();
    geographicZone.export(dto);

    return dto;
  }

  private Iterable<GeographicZoneSimpleDto> toSimpleDto(Iterable<GeographicZone> geographicZones) {
    return StreamSupport
        .stream(geographicZones.spliterator(), false)
        .map(this::toSimpleDto)
        .collect(Collectors.toList());
  }
}

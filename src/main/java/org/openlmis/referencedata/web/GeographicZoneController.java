package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
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

import java.util.UUID;

@Controller
@Transactional
public class GeographicZoneController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicZoneController.class);

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  /**
   * Allows creating new geographicZones.
   *
   * @param geographicZone A geographicZone bound to the request body.
   * @return the created geographicZone.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public GeographicZone createGeographicZone(
      @RequestBody GeographicZone geographicZone) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

    LOGGER.debug("Creating new geographicZone");
    // Ignore provided id
    geographicZone.setId(null);
    return geographicZoneRepository.save(geographicZone);
  }


  /**
   * Get all geographic zones.
   *
   * @return GeographicZones.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<GeographicZone> getAllGeographicZones(Pageable pageable) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);
    return geographicZoneRepository.findAll(pageable);
  }

  /**
   * Allows updating geographicZones.
   *
   * @param geographicZone   A geographicZone bound to the request body.
   * @param geographicZoneId UUID of geographicZone which we want to update.
   * @return the ResponseEntity containing the updated geographicZone.
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public GeographicZone updateGeographicZone(
      @RequestBody GeographicZone geographicZone, @PathVariable("id") UUID geographicZoneId) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

    LOGGER.debug("Updating geographicZone");
    return geographicZoneRepository.save(geographicZone);
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
  public GeographicZone getGeographicZone(
      @PathVariable("id") UUID geographicZoneId) {
    rightService.checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      throw new NotFoundException(GeographicZoneMessageKeys.ERROR_NOT_FOUND);
    } else {
      return geographicZone;
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
}

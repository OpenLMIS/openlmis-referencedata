package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
public class GeographicLevelController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicLevelController.class);

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  /**
   * Allows creating new geographicLevels.
   *
   * @param geographicLevel A geographicLevel bound to the request body
   * @return ResponseEntity containing the created geographicLevel
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.POST)
  public ResponseEntity<?> createGeographicLevel(@RequestBody GeographicLevel geographicLevel) {
    LOGGER.debug("Creating new geographicLevel");
    // Ignore provided id
    geographicLevel.setId(null);
    geographicLevelRepository.save(geographicLevel);
    return new ResponseEntity<>(geographicLevel, HttpStatus.CREATED);
  }

  /**
   * Get all geographicLevels.
   *
   * @return GeographicLevels.
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.GET)
  public ResponseEntity<?> getAllGeographicLevel() {
    Iterable<GeographicLevel> geographicLevels = geographicLevelRepository.findAll();
    if (geographicLevels == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicLevels, HttpStatus.OK);
    }
  }

  /**
   * Allows updating geographicLevels.
   *
   * @param geographicLevel   A geographicLevel bound to the request body
   * @param geographicLevelId UUID of geographicLevel which we want to update
   * @return ResponseEntity containing the updated geographicLevel
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateGeographicLevel(@RequestBody GeographicLevel geographicLevel,
                                                 @PathVariable("id") UUID geographicLevelId) {
    LOGGER.debug("Updating geographicLevel");
    geographicLevelRepository.save(geographicLevel);
    return new ResponseEntity<>(geographicLevel, HttpStatus.OK);
  }

  /**
   * Get chosen geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to get
   * @return geographicLevel.
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getGeographicLevel(@PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicLevel, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteGeographicLevel(@PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      geographicLevelRepository.delete(geographicLevel);
      return new ResponseEntity<GeographicLevel>(HttpStatus.NO_CONTENT);
    }
  }
}

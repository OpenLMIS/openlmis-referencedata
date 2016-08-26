package org.openlmis.referencedata.web;

import org.openlmis.referencedata.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller used for displaying service's version information.
 */
@Controller
public class VersionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionController.class);

  /**
   * Displays version information.
   *
   * @return {Version} Returns version read from file.
   */
  @RequestMapping("/version")
  public Version display() {
    LOGGER.debug("Returning version");
    return new Version();
  }
}

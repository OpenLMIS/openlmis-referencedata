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

package org.openlmis.referencedata.security;

import org.apache.commons.collections4.MapUtils;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserTokenConverter extends DefaultUserAuthenticationConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserTokenConverter.class);

  @Autowired
  private UserRepository userRepository;

  /**
   * Extracts an Authentication from a map.
   * @param map map containing information about the user.
   * @return authentication token.
     */
  public Authentication extractAuthentication(Map<String, ?> map) {
    LOGGER.info("Entering extractAuthentication");
    UsernamePasswordAuthenticationToken token =
        (UsernamePasswordAuthenticationToken) super.extractAuthentication(map);
    if (null != token) {
      String userId = MapUtils.getString(map, "referenceDataUserId", "");
      Optional<UUID> userUuid = UuidUtil.fromString(userId);
      if (userUuid.isPresent() && userRepository.exists(userUuid.get())) {
        LOGGER.info("Exiting extractAuthentication: found user " + userUuid.get());
        return new UsernamePasswordAuthenticationToken(userUuid.get(), token.getCredentials(),
            token.getAuthorities());
      }
    }
    LOGGER.info("Exiting extractAuthentication: didn't find user");
    return null;
  }
}

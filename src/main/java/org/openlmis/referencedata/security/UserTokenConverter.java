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

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Map;
import java.util.UUID;

public class UserTokenConverter extends DefaultUserAuthenticationConverter {

  @Autowired
  private UserRepository userRepository;

  /**
   * Extracts an Authentication from a map.
   * @param map map containing information about the user.
   * @return authentication token.
     */
  public Authentication extractAuthentication(Map<String, ?> map) {
    UsernamePasswordAuthenticationToken token =
        (UsernamePasswordAuthenticationToken) super.extractAuthentication(map);
    if (token != null) {
      User principal = new User();
      principal.setUsername(token.getPrincipal().toString());
      Object userId = map.get("referenceDataUserId");
      if (userId != null) {
        User user = userRepository.findOne(UUID.fromString((String) userId));
        if (user != null) {
          principal = user;
        }
      }
      return new UsernamePasswordAuthenticationToken(principal, token.getCredentials(),
          token.getAuthorities());
    }
    return null;
  }
}

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

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.SystemMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RightService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RightService.class);

  private static final String MESSAGEKEY_ERROR_UNAUTHORIZED = SystemMessageKeys.ERROR_UNAUTHORIZED;
  private static final String MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC = 
      SystemMessageKeys.ERROR_UNAUTHORIZED_GENERIC;
  
  @Autowired
  private UserRepository userRepository;

  /**
   * Check the client has the admin right specified.
   *
   * @param rightName the name of the right to check
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName) {
    checkAdminRight(rightName, true);
  }

  /**
   * Check the client has the admin right specified.
   * 
   * @param rightName the name of the right to check
   * @param allowServiceTokens whether to allow service-level tokens with root access
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName, boolean allowServiceTokens) {
    checkAdminRight(rightName, allowServiceTokens, null);
  }

  /**
   * Check the client has the admin right specified.
   *
   * @param rightName the name of the right to check
   * @param allowServiceTokens whether to allow service-level tokens with root access
   * @param expectedUserId id of the user that can bypass the right check
   *                       e.g. to retrieve his own info
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName, boolean allowServiceTokens, UUID expectedUserId) {
    LOGGER.info("Enter checkAdminRight");
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();

    if (allowServiceTokens && authentication.isClientOnly()) {
      // service-level tokens allowed and no user associated with the request
      LOGGER.info("Exit checkAdminRight: service level token found");
      return;
    } else if (!allowServiceTokens && authentication.isClientOnly()) {
      // service-level tokens not allowed and no user associated with the request
      throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName));
    } else { // user-based client, check if user has right
      UUID userId = (UUID) authentication.getPrincipal();
      User user = userRepository.findOne(userId);

      // bypass the right check if user id matches
      if (null != expectedUserId && expectedUserId.equals(user.getId())) {
        LOGGER.info("Exit checkAdminRight: user found");
        return;
      }

      if (user.hasRight(
          new RightQuery(Right.newRight(rightName, RightType.GENERAL_ADMIN)))) {
        LOGGER.info("Exit checkAdminRight: user has right");
        return;
      }
    }

    // at this point, token is unauthorized
    throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName));
  }

  /**
   * Check the client is a trusted client ("root" access).
   */
  public void checkRootAccess() {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication.isClientOnly()) { // trusted client
      return;
    }

    // at this point, token is unauthorized
    throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC));
  }
}

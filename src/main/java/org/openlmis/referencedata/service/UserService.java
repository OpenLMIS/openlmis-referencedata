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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private FacilityRepository facilityRepository;
  
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Method returns all users with matched parameters.
   *
   * @param queryMap request parameters (username, firstName, lastName, email, homeFacility,
   *                 active, verified, loginRestricted) and JSON extraData.
   * @return List of users
   */
  public List<User> searchUsers(Map<String, Object> queryMap) {

    Map<String, Object> regularQueryMap = new HashMap<>(queryMap);
    Map<String, String> extraData = (Map<String, String>) regularQueryMap.remove("extraData");

    if (queryMap.containsKey("homeFacilityId")) {
      queryMap.put("homeFacility", facilityRepository.findOne(
          UUID.fromString((String) queryMap.get("homeFacilityId"))));
    }
    List<User> foundUsers = new ArrayList<>(userRepository.searchUsers(
        (String) queryMap.get("username"),
        (String) queryMap.get("firstName"),
        (String) queryMap.get("lastName"),
        (String) queryMap.get("email"),
        (Facility) queryMap.get("homeFacility"),
        (Boolean) queryMap.get("active"),
        (Boolean) queryMap.get("verified"),
        (Boolean) queryMap.get("loginRestricted")));

    if (extraData != null && !extraData.isEmpty()) {

      String extraDataString;
      try {
        extraDataString = mapper.writeValueAsString(extraData);
        List<User> extraDataUsers = userRepository.findByExtraData(extraDataString);

        if (foundUsers != null && !foundUsers.isEmpty()) {
          // intersection between two lists
          foundUsers.retainAll(extraDataUsers);
        } else {
          foundUsers = extraDataUsers;
        }
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
      }
    }

    return Optional.ofNullable(foundUsers).orElse(Collections.emptyList());
  }

}

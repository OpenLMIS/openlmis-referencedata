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
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.RightMessageKeys;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
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
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;
  
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

        if (!foundUsers.isEmpty()) {
          // intersection between two lists
          foundUsers.retainAll(extraDataUsers);
        } else {
          foundUsers = extraDataUsers;
        }
      } catch (JsonProcessingException jpe) {
        LOGGER.error("Cannot serialize extra data query request body into JSON", jpe);
      }
    }

    return Optional.ofNullable(foundUsers).orElse(Collections.emptyList());
  }

  /**
   * Searches for users based having the specified right. The params are required
   * based on the type of the right.
   * @param rightId the ID of the right, always required
   * @param programId the ID of the program, required for supervision rights
   * @param supervisoryNodeId the ID of the supervisory node,
   *                          required for supervision rights
   * @param warehouseId the ID of the warehouse, required for fulfillment rights
   * @return users with the right assigned, matching the criteria
   */
  public Set<User> rightSearch(UUID rightId, UUID programId, UUID supervisoryNodeId,
                               UUID warehouseId) {

    Right right = rightRepository.findOne(rightId);

    if (right == null) {
      throw new ValidationMessageException(RightMessageKeys.ERROR_NOT_FOUND);
    }

    if (right.getType() == RightType.ORDER_FULFILLMENT) {
      return searchByFullfilmentRight(right, warehouseId);
    } else if (right.getType() == RightType.SUPERVISION) {
      return searchBySupervisionRight(right, supervisoryNodeId, programId);
    } else {
      return userRepository.findUsersByDirectRight(right);
    }
  }

  private Set<User> searchByFullfilmentRight(Right right, UUID warehouseId) {
    if (warehouseId == null) {
      throw new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND);
    }

    Facility warehouse = facilityRepository.findOne(warehouseId);

    if (warehouse == null) {
      throw new ValidationMessageException(new Message(
          FacilityMessageKeys.ERROR_NOT_FOUND_WITH_ID, warehouseId));
    }
    if (!warehouse.isWarehouse()) {
      throw new ValidationMessageException(
          FacilityMessageKeys.ERROR_MUST_BE_WAREHOUSE);
    }

    return userRepository.findUsersByFulfillmentRight(right, warehouse);
  }

  private Set<User> searchBySupervisionRight(Right right, UUID supervisoryNodeId,
                                             UUID programId) {
    if (supervisoryNodeId == null) {
      throw new ValidationMessageException(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);
    }
    if (programId == null) {
      throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
    }

    SupervisoryNode supervisoryNode = supervisoryNodeRepository
        .findOne(supervisoryNodeId);

    if (supervisoryNode == null) {
      throw new ValidationMessageException(new Message(
          SupervisoryNodeMessageKeys.ERROR_NOT_FOUND, supervisoryNodeId));
    }

    Program program = programRepository.findOne(programId);

    if (program == null) {
      throw new ValidationMessageException(new Message(
          ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID, programId));
    }

    return userRepository.findSupervisingUsersBy(right, supervisoryNode, program);
  }
}

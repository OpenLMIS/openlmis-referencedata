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

import java.util.UUID;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.custom.SupervisoryNodeRedisRepository;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SupervisoryNodeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisoryNodeService.class);

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private SupervisoryNodeRedisRepository supervisoryNodeRedisRepository;

  /**
   * Get chosen supervisoryNode.
   *
   * @param supervisoryNodeId UUID of the supervisoryNode whose we want to get.
   * @return the SupervisoryNode.
   */
  public SupervisoryNode getSupervisoryNode(UUID supervisoryNodeId) {
    Profiler profiler = new Profiler("GET_SUPERVISORY_NODE_SERVICE");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_IF_SUPERVISORY_NODE_EXISTS_IN_CACHE");

    SupervisoryNode supervisoryNode;
    boolean supervisoryNodeIsInCache = supervisoryNodeRedisRepository
        .existsInCache(supervisoryNodeId);

    if (supervisoryNodeIsInCache) {
      profiler.start("GET_SUPERVISORY_NODE_FROM_CACHE");
      supervisoryNode = supervisoryNodeRedisRepository.findById(supervisoryNodeId);
    } else if (!supervisoryNodeRepository.exists(supervisoryNodeId)) {
      profiler.stop().log();
      throw new NotFoundException(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);
    } else {
      profiler.start("GET_SUPERVISORY_NODE_FROM_DATABASE");
      supervisoryNode = supervisoryNodeRepository.findOne(supervisoryNodeId);
      supervisoryNodeRedisRepository.save(supervisoryNode);
    }

    return supervisoryNode;
  }

  /**
   * Allows updating supervisoryNode.
   *
   * @param supervisoryNodeToUpdate A supervisoryNodeToUpdate which we want to update.
   */
  public void updateSupervisoryNode(SupervisoryNode supervisoryNodeToUpdate) {
    Profiler profiler = new Profiler("UPDATE_SUPERVISORY_NODE_SERVICE");

    profiler.start("DELETE_UPDATED_SUPERVISORY_NODE_FROM_CACHE");
    supervisoryNodeRedisRepository.delete(supervisoryNodeToUpdate);

    profiler.start("SAVE_SUPERVISORY_NODE_IN_DATABASE");
    supervisoryNodeRepository.saveAndFlush(supervisoryNodeToUpdate);
  }

  /**
   * Allows deleting supervisoryNode.
   *
   * @param supervisoryNode A supervisoryNode which we want to delete.
   */
  public ResponseEntity deleteSupervisoryNode(SupervisoryNode supervisoryNode) {
    if (supervisoryNode == null) {
      throw new NotFoundException(SupervisoryNodeMessageKeys.ERROR_NOT_FOUND);
    } else {
      supervisoryNodeRepository.delete(supervisoryNode);
      supervisoryNodeRedisRepository.delete(supervisoryNode);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}

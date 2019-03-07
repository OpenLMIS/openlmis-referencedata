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

package org.openlmis.referencedata.repository.custom.impl;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.custom.SupervisoryNodeRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SupervisoryNodeRedisRepositoryImpl implements SupervisoryNodeRedisRepository {

  private static final String HASH_KEY = "SUPERVISORY_NODE";

  private RedisTemplate<String, SupervisoryNode> redisTemplate;
  private HashOperations hashOperations;

  @Autowired
  public SupervisoryNodeRedisRepositoryImpl(RedisTemplate<String, SupervisoryNode> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostConstruct
  private void init() {
    hashOperations = redisTemplate.opsForHash();
  }

  @Override
  public boolean existsInCache(UUID supervisoryNodeId) {
    return hashOperations.hasKey(supervisoryNodeId.toString(), HASH_KEY);
  }

  @Override
  public SupervisoryNode findById(UUID supervisoryNodeId) {
    return (SupervisoryNode)hashOperations
        .get(HASH_KEY, supervisoryNodeId.toString());
  }

  @Override
  public void save(SupervisoryNode supervisoryNode) {
    if (null != supervisoryNode) {
      hashOperations.put(HASH_KEY, supervisoryNode.getId(), supervisoryNode);
      redisTemplate.expire(HASH_KEY, 24, TimeUnit.HOURS);
    }
  }

  @Override
  public void delete(SupervisoryNode supervisoryNode) {
    if (null != supervisoryNode) {
      hashOperations.delete(HASH_KEY, supervisoryNode.getId().toString());
    }
  }
}

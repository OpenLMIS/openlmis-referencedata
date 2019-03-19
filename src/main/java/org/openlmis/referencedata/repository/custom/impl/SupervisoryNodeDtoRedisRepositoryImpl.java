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
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.repository.custom.BaseRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SupervisoryNodeDtoRedisRepositoryImpl extends BaseRedisRepositoryUtil
    implements BaseRedisRepository<SupervisoryNodeDto> {

  private static final String HASH_KEY = "SUPERVISORY_NODE_DTO";

  @Autowired
  public SupervisoryNodeDtoRedisRepositoryImpl(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean exists(UUID supervisoryNodeDtoId) {
    return hashOperations.hasKey(HASH_KEY, supervisoryNodeDtoId.toString());
  }

  @Override
  public SupervisoryNodeDto findById(UUID supervisoryNodeDtoId) {
    return mapper.convertValue(this.redisTemplate.opsForHash()
        .get(HASH_KEY, supervisoryNodeDtoId), SupervisoryNodeDto.class);
  }

  @Override
  public void save(SupervisoryNodeDto supervisoryNodeDto) {
    hashOperations.put(HASH_KEY, supervisoryNodeDto.getId(), supervisoryNodeDto);
  }

  @Override
  public void delete(SupervisoryNodeDto supervisoryNodeDto) {
    hashOperations.delete(HASH_KEY, supervisoryNodeDto.getId().toString());
  }
}

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
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.BaseRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProgramRedisRepositoryImpl extends BaseRedisRepositoryUtil
    implements BaseRedisRepository<Program> {

  private static final String HASH_KEY = "PROGRAM";

  @Autowired
  public ProgramRedisRepositoryImpl(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean exists(UUID programId) {
    return hashOperations.hasKey(HASH_KEY, programId.toString());
  }

  @Override
  public Program findById(UUID programId) {
    return mapper.convertValue(this.redisTemplate.opsForHash()
        .get(HASH_KEY, programId), Program.class);
  }

  @Override
  public void save(Program program) {
    hashOperations.put(HASH_KEY, program.getId(), program);
  }

  @Override
  public void delete(Program program) {
    hashOperations.delete(HASH_KEY, program.getId().toString());
  }
}
